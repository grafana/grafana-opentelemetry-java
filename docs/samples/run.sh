#!/bin/bash

set -euox pipefail

if [[ ! -f ./target/rolldice.jar ]]; then
	./mvnw clean package
fi

# snippet java-download-distribution
opentelemetry_javaagent_version=2.17.0
jar=opentelemetry-javaagent-${opentelemetry_javaagent_version}.jar

if [[ ! -f ./${jar} ]]; then
	curl -vL https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${opentelemetry_javaagent_version}/opentelemetry-javaagent.jar -o ${jar} # editorconfig-checker-disable-line
fi
# snippet java-download-distribution

# snippet java-run-application
export OTEL_RESOURCE_ATTRIBUTES="service.name=rolldice,service.instance.id=127.0.0.1:8080"

java -Dotel.metric.export.interval=500 -Dotel.bsp.schedule.delay=500 -javaagent:${jar} -jar ./target/rolldice.jar
# snippet java-run-application
