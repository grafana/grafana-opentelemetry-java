# Based on https://github.com/open-telemetry/opentelemetry-operator/blob/main/autoinstrumentation/java/Dockerfile
FROM busybox@sha256:1487d0af5f52b4ba31c7e465126ee2123fe3f2305d638e7827681e7cf6c83d5e

COPY agent/build/libs/grafana-opentelemetry-java.jar /javaagent.jar

RUN chmod -R go+r /javaagent.jar
