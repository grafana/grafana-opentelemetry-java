## Building
   
1. Check out https://github.com/zeitlinger/opentelemetry-java-instrumentation/tree/http-client-response-consumer
2. Run `./gradlew publishToMavenLocal` in the root directory of the project (doesn't work yet)
3. Go to the root directory of this project
4. Use the snapshot version in https://github.com/grafana/grafana-opentelemetry-java/blob/et-phone-home/build.gradle
    (doesn't work yet)
5. Run `./gradlew clean assemble`
6. Copy `agent/build/libs/grafana-opentelemetry-java.jar` to wherever you want to use the agent jar,
    e.g. `cp agent/build/libs/grafana-opentelemetry-java.jar ~/source/docker-otel-lgtm/examples/java`

## Running

1. Either [build the grafana java distro](#building) or download the
   [latest version](https://drive.google.com/file/d/1koGEMrqZZO6PEcD-hqYJvXYXLy_iCVM3/view?usp=sharing)
2. Check out https://github.com/grafana/docker-otel-lgtm/tree/hackathon-et-phone-home
3. The `grafana-opentelemetry-java.jar` from the previous step should be in the `examples/java` directory 
4. `run-lgtm.sh`
5. `run-with-compile-sdk.sh` (in `examples/java`) (or `run-vanilla.sh` if you don't want to use the ET agent)
6. `generate-traffic.sh`

## Debugging

- Provoke an error: `curl -s http://localhost:8081/shop?customer=error`
- `run-with-compile-sdk.sh` has options to attach a debugger

