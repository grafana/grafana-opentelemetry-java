# Based on https://github.com/open-telemetry/opentelemetry-operator/blob/main/autoinstrumentation/java/Dockerfile
FROM busybox@sha256:fba0711bd6995f7e0158f397d85f63998b4c8b1a1e3b1e9e0394c7b165585440

COPY agent/build/libs/grafana-opentelemetry-java.jar /javaagent.jar

RUN chmod -R go+r /javaagent.jar
