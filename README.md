# Delivery Service

Short description

This service coordinates delivery: it consumes preparation and payment events, then publishes ready/delivered events. It also exposes a manual deliver endpoint to mark an order as delivered.

Key info
- Java: 21
- Spring Boot: 3.5.7
- Kafka topics used: `order-prepared`, `payment-completed`, `payment-failed`, `order-ready-for-delivery`, `order-delivered`
- HTTP endpoints: `POST /api/delivery/{orderId}/deliver`
- Swagger UI (OpenAPI): `/swagger-ui.html` or `/swagger-ui/index.html`

Prerequisites
- Java 21 installed
- Maven or use `mvnw.cmd`
- Kafka cluster

Configuration
- Kafka bootstrap and group id in `src/main/resources/env.properties`.

Defaults in `env.properties`:

```
BOOTSTRAP_SERVER=ec2-13-201-72-156.ap-south-1.compute.amazonaws.com:9092
GROUP_ID=delivery-service-group
```

- `application.yml` sets server port `8084` by default.

Build and run

```bash
cd delivery-service-main
mvnw.cmd -DskipTests package
java -jar target/delivery-service-0.0.1-SNAPSHOT.jar
```

Swagger and API docs

- http://localhost:8084/v3/api-docs
- http://localhost:8084/swagger-ui.html
- http://localhost:8084/swagger-ui/index.html

HTTP API

Trigger manual delivery
- URL: POST /api/delivery/{orderId}/deliver
- Example:

```bash
curl -X POST http://localhost:8084/api/delivery/order-123/deliver
```

Kafka

- Consumes: `order-prepared`, `payment-completed`
- Publishes: `order-ready-for-delivery`, `order-delivered`

Troubleshooting

- If deliveries do not occur automatically, ensure both the `order-prepared` and `payment-completed` events for the order are present in Kafka.
- If Swagger doesn't load, confirm the module has `springdoc` dependency (it does in the POM) and service is up.


