# Based on https://github.com/open-telemetry/opentelemetry-operator/blob/main/autoinstrumentation/java/Dockerfile
FROM busybox@sha256:2383baad1860bbe9d8a7a843775048fd07d8afe292b94bd876df64a69aae7cb1

COPY agent/build/libs/grafana-opentelemetry-java.jar /javaagent.jar

RUN chmod -R go+r /javaagent.jar
