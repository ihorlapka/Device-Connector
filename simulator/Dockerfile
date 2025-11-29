FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY simulator/pom.xml .
COPY simulator/src ./src
RUN mvn -pl simulator -am -B -U -DskipTests package
