# Changelog
        
## 0.31.0 (2023-11-13)
                        
- This is the **first public preview release** of this distribution which is released together with the
  public release of 
  [Grafana Cloud Application Observability](https://grafana.com/docs/grafana-cloud/monitor-applications/application-observability/).
- This release is based on OpenTelemetry 1.31.0 - so the version number becomes 0.31.0 
  (we'll switch to 1.x.y with the first GA release).
- No functional changes.

## 0.3.0 (2023-11-08)

- Add resource detectors for Google Cloud and AWS - mainly for Kubernetes monitoring.
- Add ability to drop metrics that are not needed for Application Observability ([docs](README.md#data-saver)).
- Rename grafana cloud environment variables `GRAFANA_OTLP_CLOUD_*` to `GRAFANA_CLOUD_*`.

## 0.2.0 (2023-10-27)

- Update to OpenTelemetry 1.31.0
