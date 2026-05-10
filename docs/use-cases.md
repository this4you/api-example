# Use Cases: мапа слайдів лекції на код проекту

Цей файл — путівник для викладача. Для кожного слайду з [`lecture.md`](./lecture.md)
вказано: які концепції він охоплює, де в коді їх дивитися та як швидко показати в дії.

## Швидкий старт

```bash
./gradlew bootRun                                 # запустити додаток
docker compose up -d                              # опціонально: Kafka + RabbitMQ
```

Корисні URL-и:
- http://localhost:8080/ — головна з посиланнями
- http://localhost:8080/swagger-ui.html — Swagger UI (Слайд 9)
- http://localhost:8080/graphiql — GraphQL Playground (Слайди 4, 9)
- http://localhost:8080/api-demo.html — інтерактивне JWT/REST демо
- http://localhost:8080/chat.html — WebSocket чат (Слайд 5)
- http://localhost:8080/ws-services/books.wsdl — SOAP WSDL (Слайд 4)
- gRPC: `localhost:9090` (Слайд 4)

---

## Слайд 1 — Вступ: що таке API та інтеграції

**Концепції**: API як офіціант, інтеграція сервісів, повторне використання логіки.

**Файли**:
- `src/main/kotlin/.../integration/ExternalApiClient.kt` — наш сервіс викликає
  публічний `jsonplaceholder.typicode.com` через `RestClient`. Аналог "карта на сайті
  через Google Maps API".

**Як показати**:
1. Відкрити `GET http://localhost:8080/integration/external-posts` у браузері.
2. Пояснити: наш бекенд — це водночас сервер для frontend і клієнт зовнішнього API.

---

## Слайд 2 — Компоненти API

**Концепції**: endpoint, контракт, JSON, схема, клієнт/сервер, документація.

**Файли**:
- `books/v1/BookDtoV1.kt` — контракт (DTO + Bean Validation: `@NotBlank`,
  `@Pattern`, `@Min`).
- `books/v1/BookControllerV1.kt` — конкретні endpoints (`/api/v1/books`).
- `config/OpenApiConfig.kt` — описує API формально (OpenAPI).

**Як показати**:
1. Відкрити Swagger UI — студенти бачать список endpoints і моделі.
2. У Swagger натиснути на схему `BookRequestV1` → видно `pattern: \d{13}` для ISBN.
3. У `01-books-v1.http` запустити POST з невалідними даними → 400 з полями помилок.

---

## Слайд 3 — Типи API за доступом

**Концепції**: Public / Partner / Internal / Private API.

**Файли**:
- `config/SecurityConfig.kt` — три рівні доступу:
  - `/api/public/**` → permitAll (Public),
  - `/api/partner/**` → ROLE_PARTNER (Partner, через `X-API-Key`),
  - `/api/v1/**`, `/api/v2/**` → JWT (Internal),
  - `/actuator/**` → publically opened для демо, у production обмеж.
- `auth/ApiKeyAuthFilter.kt` — реалізація Partner API ключів.

**Як показати**:
1. `GET /integration/external-posts` без жодних заголовків → 200 (public).
2. `GET /api/v1/books` без токена → 401 (internal).
3. Згадати: Instagram / TikTok мають private API — недоступні стороннім.

---

## Слайд 4 — Архітектури API

**Концепції**: REST, SOAP, GraphQL, gRPC.

**Файли**:
- REST: `books/v1/BookControllerV1.kt`, `books/v2/BookControllerV2.kt`.
- SOAP: `soap/BookSoapEndpoint.kt`, `xsd/books.xsd`, `config/SoapConfig.kt`.
- GraphQL: `graphql/BookGraphQLController.kt`, `graphql/schema.graphqls`.
- gRPC: `grpc/BookGrpcService.kt`, `proto/book.proto`.

**Як показати** (порівняння однієї і тієї ж операції GET /books в 4 стилях):
1. **REST**: `GET /api/v1/books` через Swagger — JSON, HTTP, простий.
2. **GraphQL**: в `/graphiql` ввести `{ books { title } }` — клієнт обрав лише `title`.
3. **SOAP**: відкрити `/ws-services/books.wsdl` — побачити WSDL (XML контракт).
   Виклик через curl:
   ```bash
   curl -X POST http://localhost:8080/ws-services \
     -H 'Content-Type: text/xml' \
     --data '<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:b="http://this4you/apiexample/soap/books"><soap:Body><b:GetBookRequest><b:id>1</b:id></b:GetBookRequest></soap:Body></soap:Envelope>'
   ```
4. **gRPC**: бінарний RPC. Перевірити через `grpcurl`:
   ```bash
   brew install grpcurl
   grpcurl -plaintext -d '{}' localhost:9090 book.BookGrpcApi/List
   grpcurl -plaintext -d '{"id":1}' localhost:9090 book.BookGrpcApi/Get
   ```

---

## Слайд 5 — Протоколи та формати даних

**Концепції**: HTTP/HTTPS, WebSocket, JSON/XML/Protobuf, Content-Type, серіалізація.

**Файли**:
- HTTP — будь-який REST controller (Spring MVC = HTTP).
- WebSocket: `chat/ChatWebSocketHandler.kt`, `config/WebSocketConfig.kt`, `static/chat.html`.
- JSON ↔ Kotlin: Jackson (default Spring) — `application/json`.
- XML ↔ Kotlin: JAXB у `soap/SoapDto.kt` — `application/xml` / `text/xml`.
- Protobuf: `proto/book.proto` → бінарні класи в `build/generated/source/proto`.

**Як показати**:
1. Відкрити `/chat.html` у двох вкладках — побачити broadcast у real-time.
2. Пояснити різницю: HTTP — request/response, WebSocket — постійне з'єднання.
3. Показати в `BookControllerV1.kt` Content-Type negotiation (Spring сам обирає).

---

## Слайд 6 — RESTful принципи

**Концепції**: ресурс/URI, HTTP методи, stateless, кешування, уніформний інтерфейс.

**Файли**:
- `books/v1/BookControllerV1.kt` — кожен принцип у дії:
  - URL описує ресурс (`/books/{id}`), а не дію.
  - GET/POST/PUT/DELETE = семантика дії.
  - `cacheControl(maxAge(30s))` → `Cache-Control` header.
  - 201 Created + `Location` header при створенні.
  - 204 No Content при DELETE.
- `config/SecurityConfig.kt` — `SessionCreationPolicy.STATELESS` (нема сесій).

**Як показати**:
1. У `01-books-v1.http`: GET → 200, POST → 201 + Location, DELETE → 204.
2. В Network tab браузера побачити `Cache-Control: max-age=30, public`.

---

## Слайд 7 — Аутентифікація й авторизація

**Концепції**: API key, Basic Auth, OAuth2, JWT, mTLS.

**Файли**:
- JWT: `auth/JwtService.kt`, `auth/JwtAuthFilter.kt`, `auth/AuthController.kt`.
- API Key: `auth/ApiKeyAuthFilter.kt`.
- Basic Auth: увімкнено в `config/SecurityConfig.kt` (`http.httpBasic`).
- OAuth2 / mTLS: лише пояснення в коментарях (повноцінна реалізація — окрема тема).

**Як показати**:
1. `03-auth.http` → POST /auth/login (admin/admin) → отримати JWT.
2. У Swagger UI натиснути "Authorize" → вставити токен → захищені endpoints.
3. `04-orders-errors.http` показати X-API-Key header — для Partner API.
4. Згадати OAuth2: "Увійти через Google" — у нас не реалізовано, але це той самий
   потік: користувач → IdP → access token → наш API.

---

## Слайд 8 — Версіонування API

**Концепції**: URI versioning, header versioning, backward compatibility, deprecation.

**Файли**:
- `books/v1/` vs `books/v2/` — два пакети, два контракти.
- `BookControllerV1.kt` додає `Deprecation: true`, `Sunset: 2026-12-31`,
  `Link: </api/v2/books>; rel="successor-version"`.

**Як показати**:
1. `01-books-v1.http` GET /api/v1/books — в headers побачити `Deprecation`.
2. `02-books-v2.http` GET /api/v2/books — той самий ресурс, новий контракт
   (`price.amount`, `price.currency`, `tags`).
3. Пояснити: старі клієнти не падають, поки v1 живий.

---

## Слайд 9 — Документація та специфікації

**Концепції**: OpenAPI/Swagger, GraphQL schema/GraphiQL, SDK generation, changelog.

**Файли**:
- `config/OpenApiConfig.kt` — мета-інформація API.
- `@Operation`, `@Tag`, `@Schema` анотації в контролерах і DTO.
- `graphql/schema.graphqls` — описова схема GraphQL.

**Як показати**:
1. Swagger UI: http://localhost:8080/swagger-ui.html — тут можна одразу клікнути
   "Try it out".
2. GraphiQL: http://localhost:8080/graphiql — автокомпліт полів зі schema.
3. SDK: пояснити, що `openapi-generator` згенерує клієнт під будь-яку мову з
   `/api-docs`. Команда:
   `openapi-generator-cli generate -i http://localhost:8080/api-docs -g typescript-fetch`.

---

## Слайд 10 — Обробка помилок і статус-коди

**Концепції**: 2xx/4xx/5xx, структуровані тіла помилок, retry/backoff, traceId.

**Файли**:
- `common/ErrorResponse.kt` — стандартний JSON помилки.
- `common/GlobalExceptionHandler.kt` — мапінг exception → HTTP статус.
- `common/CorrelationIdFilter.kt` — `X-Trace-Id` + MDC + log pattern.
- Retry/backoff — обговорено в коментарях `WebhookSenderService.kt`.

**Як показати**:
1. `04-orders-errors.http` GET /orders/some-fake-id → 404 з JSON:
   ```json
   { "code": "NOT_FOUND", "message": "Order ... not found", "traceId": "...", "timestamp": "..." }
   ```
2. Заголовок `X-Trace-Id` у відповіді = traceId у логах. Спробувати передати свій
   X-Trace-Id у запиті → він з'явиться в логах.
3. Передати невалідний body для `POST /api/v1/books` → 400 з `details[]`.
4. Пояснити retry/backoff: 1s → 2s → 5s → 10s. У реальних SDK (наприклад,
   AWS SDK) це вбудовано.

---

## Слайд 11 — Асинхронні інтеграції

**Концепції**: webhook, message broker (Kafka), Pub/Sub, event-driven.

**Файли**:
- Webhook out: `webhooks/WebhookSenderService.kt` (з `@Async`).
- Webhook in: `webhooks/WebhookReceiverController.kt`.
- Kafka: `messaging/KafkaProducer.kt`, `messaging/KafkaConsumer.kt`.
- Async response: `async/AsyncController.kt`.

**Як показати**:
1. `docker compose up -d` — підняти Kafka.
2. `04-orders-errors.http` → POST /orders. У логах побачити:
   - `OrderService` логує створення,
   - `KafkaProducer` публікує, `KafkaConsumer` приймає,
   - `WebhookSenderService` викликає `/webhooks/received`,
   - `WebhookReceiverController` логує отримання.
3. `GET /api/public/async/report` — імітує повільний звіт через `CompletableFuture`.
4. Згадати, що RabbitMQ — це альтернатива Kafka для черг задач (різні моделі:
   Kafka — лог подій, Rabbit — черга з ack/reject).

---

## Слайд 12 — API Gateway та проміжне ПЗ

**Концепції**: routing, auth, rate limiting, aggregation, монiторинг.

**Файли**:
- `common/RateLimitFilter.kt` — простий rate limit (Bucket4j) як приклад того,
  що зазвичай робить Gateway.
- `config/SecurityConfig.kt` — централізована перевірка JWT/API Key (Gateway
  робить це для всіх сервісів одразу).
- `common/CorrelationIdFilter.kt` — traceId, який зазвичай створює Gateway і
  передає далі.

**Як показати**:
1. У `application.yml` змінити `demo.rate-limit.requests-per-minute: 5`,
   перезапустити, в `01-books-v1.http` 6 разів натиснути GET → 429.
2. Пояснити теоретично: у production цю функцію бере на себе Kong / NGINX / AWS API
   Gateway / Spring Cloud Gateway. Наш фільтр — спрощена ілюстрація.

---

## Слайд 13 — Кращі практики та чекліст

**Концепції**: контракти/документація, security/testing/monitoring, versioning,
key management, SLA, командна відповідальність.

**Файли**:
- Документація: `OpenApiConfig.kt`, `@Operation`/`@Schema` анотації.
- Тестування: `src/test/kotlin/.../BookControllerV1Test.kt`, `JwtServiceTest.kt`.
- Моніторинг: `spring-boot-starter-actuator`, `/actuator/health`,
  `/actuator/metrics`, формат `prometheus`.
- Версіонування: пакети `v1/` і `v2/`.
- Logging: pattern з `traceId` в `application.yml`.

**Як показати**:
1. `./gradlew test` — passing.
2. `GET /actuator/health` — UP.
3. `GET /actuator/metrics/http.server.requests` — статистика по endpoints.
4. Пройтись по чеклісту: документація ✔, security ✔, monitoring ✔, versioning ✔,
   error handling ✔, tracing ✔.

---

## Підсумкова таблиця

| Слайд | Тема                          | Ключові файли                                                    |
|-------|-------------------------------|-------------------------------------------------------------------|
| 1     | Вступ                         | `integration/ExternalApiClient.kt`                                |
| 2     | Компоненти API                | `books/v1/BookDtoV1.kt`, `config/OpenApiConfig.kt`                |
| 3     | Типи доступу                  | `config/SecurityConfig.kt`, `auth/ApiKeyAuthFilter.kt`            |
| 4     | Архітектури                   | REST/GraphQL/gRPC/SOAP контролери                                 |
| 5     | Протоколи й формати           | `chat/ChatWebSocketHandler.kt`, `proto/book.proto`, `xsd/books.xsd` |
| 6     | RESTful принципи              | `books/v1/BookControllerV1.kt`                                    |
| 7     | Аутентифікація                | `auth/*`                                                          |
| 8     | Версіонування                 | `books/v1/`, `books/v2/`                                          |
| 9     | Документація                  | `OpenApiConfig.kt`, `graphql/schema.graphqls`                     |
| 10    | Помилки                       | `common/GlobalExceptionHandler.kt`, `common/CorrelationIdFilter.kt` |
| 11    | Async інтеграції              | `webhooks/*`, `messaging/*`, `async/AsyncController.kt`           |
| 12    | API Gateway                   | `common/RateLimitFilter.kt`, `config/SecurityConfig.kt`           |
| 13    | Best practices                | `src/test/`, `application.yml` (logging+actuator)                 |
