FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY device-connector/pom.xml ./device-connector/
COPY device-connector/src ./device-connector/src
RUN mvn -pl device-connector -B -DskipTests install

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY simulator/pom.xml .
COPY simulator/src ./src
RUN mvn -pl simulator -am -B -U -DskipTests package
