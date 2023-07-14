#!/usr/bin/env bash

scriptDir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

while [[ $# -gt 0 ]]; do
  case $1 in
    -a|--attachDebugger)
      echo "attaching debugger on port 5000"
      attachDebugger=true
      shift
      ;;
    -l|--debugLogging)
      echo "logging all telemetry to stdout"
      debugLogging=true
      shift
      ;;
    -m|--debugModules)
      echo "show active instrumentations modules"
      debugModules=true
      shift
      ;;
    *)
      echo "Unknown option $1"
      exit 1
      ;;
  esac
done

"$scriptDir"/start-grafana-agent.sh

agentVersion=1.27.0
agent="opentelemetry-javaagent-$agentVersion.jar"
agentPath="$scriptDir/$agent"

if [[ ! -f "$agentPath" ]]; then
  echo "Downloading $agent"
  curl -Lo "$agentPath" https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v$agentVersion/opentelemetry-javaagent.jar
fi

jvm_args=$(cat <<-END
-javaagent:$agentPath
			-Dotel.logs.exporter=otlp
			-Dotel.instrumentation.micrometer.base-time-unit=s
			-Dotel.semconv-stability.opt-in=http
			-Dotel.instrumentation.log4j-appender.experimental-log-attributes=true
			-Dotel.instrumentation.logback-appender.experimental-log-attributes=true
			-Dotel.service.name=shopping-cart
			-Dotel.resource.attributes=deployment.environment=production,service.namespace=shop,service.version=1.1,service.instance.id=shopping-cart-66b6c48dd5-hprdn
END
)

if [[ $attachDebugger == "true" ]]; then
  address="*:5005"
  if [[ $(java -version 2>&1) =~ 1\.8\.0 ]]; then
    address="5005"
  fi
  jvm_args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=$address $jvm_args"
fi

if [[ $debugModules == "true" ]]; then
  jvm_args="$jvm_args -Dotel.javaagent.debug=true"
fi

if [[ $debugLogging == "true" ]]; then
  jvm_args="$jvm_args -Dotel.logs.exporter=otlp,logging -Dotel.metrics.exporter=otlp,logging -Dotel.traces.exporter=otlp,logging"
fi

echo "Used JVM args: $jvm_args"

"$scriptDir"/../gradlew bootRun -PjvmArgs="$jvm_args"
