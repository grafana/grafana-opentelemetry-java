# Contributing

This project is a javaagent distribution of the OpenTelemetry Java instrumentation agent.
It is modeled after the [OpenTelemetry Java instrumentation agent distro template](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/examples/distro/README.md).

## Build

```sh
./gradlew clean
```

```sh
./gradlew build
```

The build process will generate `agent/build/libs/grafana-opentelemetry-java.jar`, which contains the upstream
javaagent.jar as well as our custom extension.
                      
## Debugging

If one of the test applications in the "examples" directory fails to produce the right telemetry
(usually detected by the oats test), you can run the application with the javaagent attached to it 
by adding the following command line arguments:

```sh
./run.sh --attachDebugger --debugLogging --debugModules --enableAllModules
```

## Acceptance Tests
                          
Acceptance test cases are defined in `oats.yaml` files in the examples directory.
The test cases are run by [oats]. The declarative yaml tests are described in https://github.com/grafana/oats/blob/main/yaml.
                                                                                 
> Note that many `oats.yaml` files are symlinks to avoid repetition.

If a test case fails (lets say "examples/jdbc/spring-boot-reactive-2.7"), follows these steps:

1. Check out [oats] repo
2. install ginkgo: `go install github.com/onsi/ginkgo/ginkgo`
3. `export TESTCASE_TIMEOUT=2h && export TESTCASE_BASE_PATH=/path/to/this/repo/examples && ginkgo -v -r -focus 'jdbc-spring-boot-reactive-2'`
4. go to http://localhost:3000 and login with admin/admin
                                                                                                                                            
Use `-focus 'yaml'` to run all acceptance tests.

## Known Issues

The tests occasionally fail due to TestContainers not starting in time.  Please rerun the build for now, until
a new wait strategy can be determined.

[oats]: https://github.com/grafana/oats
