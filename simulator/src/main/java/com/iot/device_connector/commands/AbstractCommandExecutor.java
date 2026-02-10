package com.iot.device_connector.commands;

import com.google.common.cache.*;
import com.iot.device_connector.devices.DevicesProvider;
import com.iot.device_connector.devices.dto.DeviceDto;
import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import com.iot.device_connector.model.enums.DeviceType;
import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCommandExecutor<C extends SpecificRecord, D extends SpecificRecord, T extends DeviceDto> {

    private static final int COMMANDS_CAPACITY = 100;

    private final DevicesProvider devicesProvider;
    private final TelemetriesKafkaProducerRunner kafkaProducerRunner;

    private final ConcurrentHashMap<String, BlockingQueue<CommandWithExecution>> futureExecutionsByDeviceId = new ConcurrentHashMap<>();
    private final Cache<String, ExecutorService> executorsCache = buildExecutorsByDeviceIdCache();
    private final AtomicLong virtualThreadsCount = new AtomicLong();


    abstract D applyCommand(C command, D device);
    abstract boolean verifyIfShouldCancel(C oldCommand, C newCommand);
    abstract DeviceType getDeviceType();
    abstract D mapDeviceFromDtoToAvro(T device);
    abstract T mapDeviceFromAvroToDto(D device);

    Future<RecordMetadata> sendMessage(String deviceId, D device) {
        return kafkaProducerRunner.sendMessage(deviceId, device);
    }

    void updateDevicesCache(D device) {
        devicesProvider.updateDevice(mapDeviceFromAvroToDto(device));
    }

    public void submitCommand(C command, Function<C, String> deviceIdFunction) {
        try {
            final String deviceId = deviceIdFunction.apply(command);
            final BlockingQueue<CommandWithExecution> futureExecutions = futureExecutionsByDeviceId
                    .computeIfAbsent(deviceId, (k) -> new LinkedBlockingQueue<>(COMMANDS_CAPACITY));
            checkIfShouldCancelAnyRunningCommands(command, futureExecutions);

            final CommandWithExecution commandWithExecution = new CommandWithExecution(command);
            if (futureExecutions.offer(commandWithExecution)) {
                try {
                    final Future<D> futureExecution = executorsCache.get(deviceId, this::getVirtualExecutorService)
                            .submit(executeWithRemoval(command, deviceId, futureExecutions));
                    commandWithExecution.setFutureExecution(futureExecution);
                    log.info("Submitted new command for execution: {}", command);
                } catch (ExecutionException e) {
                    log.error("Failed to get executor for deviceId {}", deviceId, e);
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    if (futureExecutions.remove(commandWithExecution)) {
                        log.warn("Removed {} due to exception", commandWithExecution, e);
                    }
                    throw e;
                }
            } else {
                throw new RuntimeException("Too many commands are executing now!");
            }
        } catch (Exception e) {
            log.error("Unexpected exception occurred during command execution!");
        }
    }

    private ExecutorService getVirtualExecutorService() {
        return Executors.newSingleThreadExecutor(Thread.ofVirtual().name("virtual-" + virtualThreadsCount.getAndIncrement()).factory());
    }

    private void checkIfShouldCancelAnyRunningCommands(C newCommand, BlockingQueue<CommandWithExecution> futureExecutions) {
        final Iterator<CommandWithExecution> iterator = futureExecutions.iterator();
        while (iterator.hasNext()) {
            final CommandWithExecution commandWithExecution = iterator.next();
            if (verifyIfShouldCancel(commandWithExecution.getCommand(), newCommand)) {
                final Future<D> futureExecution = commandWithExecution.getFutureExecution();
                if (futureExecution != null) {
                    futureExecution.cancel(true);
                }
                iterator.remove();
                log.info("Cancelled and removed command from queue: {}", commandWithExecution.getCommand());
            }
        }
    }

    private Callable<D> executeWithRemoval(C command, String deviceId, BlockingQueue<CommandWithExecution> futureExecutions) {
        return () -> {
            try {
                final D device = getDevice(deviceId);
                return applyCommand(command, device);
            } catch (Exception e) {
                log.error("Failed to execute command: {}", command, e);
                throw e;
            } finally {
                futureExecutions.removeIf(c -> {
                    boolean shouldRemove = c.getCommand().equals(command);
                    if (shouldRemove) {
                        log.info("Removing command task {}", c);
                        return true;
                    }
                    return false;
                });
            }
        };
    }

    @SuppressWarnings("unchecked")
    private D getDevice(String deviceId) {
        return devicesProvider.getDevice(getDeviceType(), deviceId)
                .map(deviceDto -> mapDeviceFromDtoToAvro((T) deviceDto))
                .orElseThrow(() -> new RuntimeException("No device present!"));
    }

    private Cache<String, ExecutorService> buildExecutorsByDeviceIdCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .removalListener((RemovalNotification<String, ExecutorService> notification) -> {
                    final ExecutorService executor = notification.getValue();
                    if (executor != null) {
                        log.info("Closing executor for device: {} due to {}", notification.getKey(), notification.getCause());
                        executor.shutdown();
                        try {
                            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                                executor.shutdownNow();
                            }
                            virtualThreadsCount.decrementAndGet();
                        } catch (InterruptedException e) {
                            executor.shutdownNow();
                            Thread.currentThread().interrupt();
                        }
                    }
                })
                .build();
    }

    @Getter
    @ToString
    @EqualsAndHashCode(of = "command")
    @RequiredArgsConstructor
    class CommandWithExecution {
        @NonNull
        private final C command;
        @Setter
        @Nullable
        private Future<D> futureExecution;
    }

    @PreDestroy
    private void closeExecutors() throws InterruptedException {
        log.info("Shutting down executors...");
        for (Map.Entry<String, ExecutorService> entry : executorsCache.asMap().entrySet()) {
            final ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            boolean isShoutdown = executorService.awaitTermination(5, TimeUnit.SECONDS);
            if (!isShoutdown) {
                executorService.shutdownNow();
            }
        }
        log.info("All executors were shut down");
    }
}
