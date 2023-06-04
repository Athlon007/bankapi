FROM ubuntu:latest AS build
RUN apt-get update
RUN apt-get install openjdk-19-jdk -y
COPY . .
RUN ./mvnw compile
RUN ./mvnw exec:java -Dexec.mainClass=nl.inholland.BankApplication
