FROM ubuntu:latest AS build
RUN apt-get update
RUN apt-get install openjdk-19-jdk -y
COPY . .
RUN ./mvnw clean install -U

# Download the inholland.p12 form http://kfigura.nl/bank/security/inholland.p12
RUN wget -q https://kfigura.nl/bank/security/inholland.p12

EXPOSE 8080
ENTRYPOINT ["./mvnw","spring-boot:run"]
