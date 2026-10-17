# observability

Spring Boot Observability Sample

[![Java CI with Maven](https://github.com/hendisantika/observability/actions/workflows/maven.yml/badge.svg)](https://github.com/hendisantika/observability/actions/workflows/maven.yml)

A two-service sample that shows the three pillars of observability with Spring Boot:

- **Metrics** with Micrometer + Prometheus (`/actuator/prometheus`)
- **Traces** with Micrometer Tracing (Brave bridge) + Zipkin reporter
- **Logs** shipped to Grafana Loki with the `loki-logback-appender`, enriched with `traceId` / `spanId`

## Tech Stack

- Java 25
- Spring Boot 4.1.1 (WebFlux, Actuator)
- Micrometer (Prometheus registry, Tracing bridge Brave)
- Loki4j logback appender 2.1.0
- Lombok
- Maven 3.10 (Maven Wrapper included)

## Modules

| Module        | Port | Description                                                                                              |
|---------------|------|----------------------------------------------------------------------------------------------------------|
| `fibo-server` | 8090 | Exposes `GET /api` and `POST /api/calculate-fibos`, calculating Fibonacci numbers for a list of inputs.   |
| `fibo-client` | 8080 | On startup, sends a random `{"numbers": [...]}` request to the server every second using `WebClient`.    |

## Requirements

- JDK 25
- (Optional) Grafana, Loki, Prometheus and Zipkin/Tempo to view the telemetry

## Build

```shell
cd fibo-server && ./mvnw clean package
cd fibo-client && ./mvnw clean package
```

## Run

Start the server first, then the client:

```shell
cd fibo-server && ./mvnw spring-boot:run
cd fibo-client && ./mvnw spring-boot:run
```

Try the server manually:

```shell
curl -X POST http://localhost:8090/api/calculate-fibos \
  -H 'Content-Type: application/json' \
  -d '{"numbers": [10, 20, 30]}'
```

## Observability Endpoints

- Health: `http://localhost:8090/actuator/health`
- Prometheus metrics: `http://localhost:8090/actuator/prometheus`

Logs are pushed to Loki via the `Loki4jAppender` configured in `src/main/resources/logback.xml` of each module.
Set the Loki URL (and credentials, if needed) there to match your environment — do not commit secrets.

## CI

GitHub Actions (`.github/workflows/maven.yml`) builds and tests both modules on JDK 25 (Temurin) for every push and
pull request to `main`.
