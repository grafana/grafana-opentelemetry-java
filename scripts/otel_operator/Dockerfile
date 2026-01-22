# Based on https://github.com/open-telemetry/opentelemetry-operator/blob/main/autoinstrumentation/java/Dockerfile
FROM busybox@sha256:e226d6308690dbe282443c8c7e57365c96b5228f0fe7f40731b5d84d37a06839

COPY agent/build/libs/grafana-opentelemetry-java.jar /javaagent.jar

RUN chmod -R go+r /javaagent.jar
