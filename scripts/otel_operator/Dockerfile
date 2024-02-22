# Based on https://github.com/open-telemetry/opentelemetry-operator/blob/main/autoinstrumentation/java/Dockerfile
FROM busybox

COPY agent/build/libs/grafana-opentelemetry-java.jar /javaagent.jar

RUN chmod -R go+r /javaagent.jar
