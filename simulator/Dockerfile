FROM maven:3.9.6-eclipse-temurin-21
WORKDIR /app
COPY simulator/pom.xml .
COPY simulator/src ./src
COPY /ci/settings.xml /root/.m2/settings.xml
RUN cat /root/.m2/settings.xml
RUN mvn -B -s /root/.m2/settings.xml -U -DskipTests package
