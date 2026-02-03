package com.iot.device_connector.commands;

import com.iot.device_connector.kafka.TelemetriesKafkaProducerRunner;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCommandExecutor<C, D extends SpecificRecord> {

    private static final int COMMANDS_CAPACITY = 100;

    private final TelemetriesKafkaProducerRunner kafkaProducerRunner;

    private final ConcurrentHashMap<String, D> deviceByDeviceId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ExecutorService> executorServiceByDeviceId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BlockingQueue<CommandWithExecution>> futureExecutionsByDeviceId = new ConcurrentHashMap<>();


    abstract D applyCommand(C command, D device);
    abstract boolean verifyIfShouldCancel(C oldCommand, C newCommand);

    Future<RecordMetadata> sendMessage(String deviceId, D device) {
        return kafkaProducerRunner.sendMessage(deviceId, device);
    }

    public void submitCommand(C command, Function<C, String> deviceIdFunction) {
        final String deviceId = deviceIdFunction.apply(command);
        final D device = deviceByDeviceId.get(deviceId);
        executorServiceByDeviceId.computeIfAbsent(deviceId, (k) -> Executors.newSingleThreadExecutor(Thread.ofVirtual().name("device-" + k).factory()));

        final BlockingQueue<CommandWithExecution> futureExecutions = futureExecutionsByDeviceId.computeIfAbsent(deviceId, (k) -> new LinkedBlockingQueue<>(COMMANDS_CAPACITY));
        checkIfShouldCancelAnyRunningCommands(command, futureExecutions);

        final Future<D> futureExecution = executorServiceByDeviceId.get(deviceId).submit(executeWithRemoval(command, device, futureExecutions));
        if (!futureExecution.isDone()) {
            boolean isAdded = futureExecutions.offer(CommandWithExecution.of(command, futureExecution));
            if (!isAdded) {
                futureExecution.cancel(true);
                throw new RuntimeException("Too many commands are executing now!");
            } else {
                log.info("Submitted new command for execution: {}", command);
            }
        }
    }

    private void checkIfShouldCancelAnyRunningCommands(C newCommand, BlockingQueue<CommandWithExecution> futureExecutions) {
        for (CommandWithExecution commandWithExecution : futureExecutions) {
            if (verifyIfShouldCancel(commandWithExecution.getCommand(), newCommand)) {
                commandWithExecution.getFutureExecution().cancel(true);
            }
        }
    }

    private Callable<D> executeWithRemoval(C command, D device, BlockingQueue<CommandWithExecution> futureExecutions) {
        return () -> {
            try {
                return applyCommand(command, device);
            } finally {
                futureExecutions.removeIf(c -> c.getCommand().equals(command));
            }
        };
    }

    @Value(staticConstructor = "of")
    class CommandWithExecution {
        C command;
        Future<D> futureExecution;
    }

    @PreDestroy
    private void closeExecutors() throws InterruptedException {
        log.info("Shutting down executors...");
        for (Map.Entry<String, ExecutorService> entry : executorServiceByDeviceId.entrySet()) {
            String k = entry.getKey();
            ExecutorService v = entry.getValue();
            v.shutdown();
            boolean isShoutdown = v.awaitTermination(5, TimeUnit.SECONDS);
            if (!isShoutdown) {
                v.shutdownNow();
            }
        }
        log.info("All executors were shut down");
    }
}
