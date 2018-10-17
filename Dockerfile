FROM maven:3-jdk-8-alpine as builder
WORKDIR /root/
COPY ./ ./
RUN mvn package

FROM openjdk:8-alpine
WORKDIR /root/
COPY --from=builder /root/target/prometheus-disk-space-1.0-SNAPSHOT.jar .
CMD ["/usr/bin/java","-jar", "/root/prometheus-disk-space-1.0-SNAPSHOT.jar"]  