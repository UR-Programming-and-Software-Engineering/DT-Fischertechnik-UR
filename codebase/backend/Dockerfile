FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml ./
COPY .mvn/ .mvn/
COPY src/ src/

#pre-download dependencies
RUN mvn dependency:go-offline

ENV SERVER_PORT=8080
CMD ["mvn","spring-boot:run","-Dspring-boot.run.arguments=--server.port=8080"]

EXPOSE 8080
