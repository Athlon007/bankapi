FROM ubuntu:latest AS build
RUN apt-get update
RUN apt-get install openjdk-19-jdk -y
COPY . .
RUN ./mvnw clean install -U
RUN ls 
RUN echo =======
RUN ls target

FROM openjdk:19-jdk-slim
EXPOSE 8443
#COPY --from=build target/bank-0.0.1-SNAPSHOT.jar app.jar
RUN ./mvnw spring-boot:run

