FROM ubuntu:latest AS build
RUN apt-get update
RUN apt-get install openjdk-19-jdk -y
COPY . .
RUN ./mvnw clean install -U

FROM openjdk:19-jdk-slim
EXPOSE 8443
RUN ./mvnw spring-boot:run

