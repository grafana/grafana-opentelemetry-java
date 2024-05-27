#!/usr/bin/env bash

scriptDir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

while [[ $# -gt 0 ]]; do
  case $1 in
    --includeAllInstrumentations)
      echo "including all instrumentations"
      includeAllInstrumentations=true
      shift
      ;;
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
    -m|--debugInstrumentations)
      echo "show active instrumentations instrumentations"
      debugInstrumentations=true
      shift
      ;;
    *)
      echo "Unknown option $1"
      exit 1
      ;;
  esac
done

"$scriptDir"/start-grafana-agent.sh

agentVersion=2.4.0-beta.1
agent="grafana-opentelemetry-java-$agentVersion.jar"
agentPath="$scriptDir/$agent"

if [[ ! -f "$agentPath" ]]; then
  echo "Downloading $agent"
  # this still needs a token until the project is public
  curl -Lo "$agentPath" https://github.com/grafana/grafana-opentelemetry-java/releases/download/v$agentVersion/grafana-opentelemetry-java.jar
fi

jvm_args=$(cat <<-END
-javaagent:$agentPath -Dotel.resource.attributes=deployment.environment=production,service.namespace=shop,service.version=1.1 -Dotel.metric.export.interval=10000
END
)

if [[ $attachDebugger == "true" ]]; then
  address="*:5005"
  if [[ $(java -version 2>&1) =~ 1\.8\.0 ]]; then
    address="5005"
  fi
  jvm_args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=$address $jvm_args"
fi
if [[ $includeAllInstrumentations != "true" ]]; then
  jvm_args="$jvm_args -Dgrafana.otel.use-tested-instrumentations=true"
fi
if [[ $debugInstrumentations == "true" ]]; then
  jvm_args="$jvm_args -Dotel.javaagent.debug=true"
fi
if [[ $debugLogging == "true" ]]; then
  jvm_args="$jvm_args -Dotel.logs.exporter=otlp,console -Dotel.metrics.exporter=otlp,console -Dotel.traces.exporter=otlp,console"
fi

echo "Used JVM args: $jvm_args"

"$scriptDir"/../gradlew bootRun -PjvmArgs="$jvm_args"
