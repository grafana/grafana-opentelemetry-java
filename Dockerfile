# Example quickstart Dockerfile:
# - Spring petclinic:           https://github.com/spring-projects/spring-petclinic
# - Grafana Cloud OTLP Gateway: https://github.com/grafana/grafana-opentelemetry-java#grafana-cloud-otlp-gateway
# For production, use Dockerfile.production instead.

FROM springio/petclinic

# 1. Sign in to Grafana Cloud (https://grafana.com), register for a Free Grafana Cloud account if required.
#
# 2. After successful login, the browser will navigate to the Grafana Cloud Portal page https://grafana.com/profile/org.
#
#    A new account will most likely belong to one organization with one stack.
#
#    If the account has access to multiple Grafana Cloud Organizations, select an organization from the top left **organization dropdown**.
#
#    If the organization has access to multiple Grafana Cloud Stacks, navigate to a stack from the **left side bar** or the main **Stacks** list.
#
# 3. With a stack selected, or in the single stack scenario, below **Manage your Grafana Cloud Stack**, click **Configure** in the **OpenTelemetry** section.
#
# 4. In the **Password / API Token** section, click on **Generate now** to create a new API token:
#    - Give the API token a name, for example `otel-java`
#    - Click on **Create token**
#    - Click on **Close** without copying the token
#    - Now the environment variables section is populated with all the necessary information to send telemetry data to Grafana Cloud
#    - Click on **Copy to Clipboard** to and replace the three lines below, replacing "export " with "ENV " (without the quotes)

ENV OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="https://otlp-gateway-prod-eu-west-0.grafana.net/otlp"
ENV OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic secret"

# 5. Come up with a **Service Name** to identify the service, for example `cart`, and copy it into the shell command below.
#    Use the `service.namespace` to group multiple services together.

ENV OTEL_SERVICE_NAME=<Service Name>

# 6. Optional: add resource attributes to the shell command below:
#    - **deployment.environment**: Name of the deployment environment, for example `staging` or `production`
#    - **service.namespace**: A namespace to group similar services, for example using `service.namespace=shop` for a `cart`
#                             and `fraud-detection` service would create `shop/cart` and `shop/fraud-detection` in
#                             Grafana Cloud Application Observability with filtering capabilities for easier management
#    - **service.version**: The application version, to see if a new version has introduced a bug
#    - **service.instance.id**: The unique instance, for example the Pod name (a UUID is generated by default)
#
# ENV OTEL_RESOURCE_ATTRIBUTES=deployment.environment=<Environment>,service.namespace=<Namespace>,service.version=<Version>

# Build and run the application:
#  7. docker build -t grafana_opentelemetry_java_demo .
#  8. docker run -p 8080:8080 grafana_opentelemetry_java_demo
#  9. Open <http://localhost:8080> in your browser
# 10. Click on "Error" in the top right corner to generate an error
# 11. Open Application Observability in Grafana Cloud:
#     - Click on the menu icon in the top left corner
#     - Open the "Observability" menu
#     - Click on "Application"
# Note: It might take up to five minutes for data to appear.

ARG version=2.0.0-beta.1
WORKDIR /app/

# use a fixed version
# ADD https://github.com/grafana/grafana-opentelemetry-java/releases/download/v$version/grafana-opentelemetry-java.jar /app/grafana-opentelemetry-java.jar
# use the latest version
# user is changed, because the springio/petclinic image is running as cnb
ADD --chown=cnb https://github.com/grafana/grafana-opentelemetry-java/releases/latest/download/grafana-opentelemetry-java.jar /app/grafana-opentelemetry-java.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/app/grafana-opentelemetry-java.jar
