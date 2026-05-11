# API Demo Lab — лекція з API та інтеграцій

Привіт! Це інтерактивна лекція. Тут ти **сам(а) запускаєш** код,
викликаєш API різних типів і бачиш, як це працює зсередини. Викладач допомагає
з питаннями, а ти проходиш матеріал у власному темпі.

Усі завдання прив'язані до текстової частини лекції — її повний скрипт лежить
у [`docs/lecture.md`](docs/lecture.md). У цьому README — практика: команди,
посилання на код, експерименти.

---

## Як працює лекція

1. Запускаєш проект (один раз на початку).
2. Проходиш **практичні розділи** одну за одною, у порядку нумерації.
3. У кожному розділі є три блоки:
   - **Що це?** — коротка теорія (нагадування зі слайду).
   - **Подивись код** — конкретні файли, які треба відкрити в IntelliJ.
   - **Спробуй сам** — конкретні команди, які треба виконати.
4. Якщо щось не зрозуміло — питай викладача.

Не пропускай порядок: Практика 3 (Auth) видає токен, який потрібен у Практиках 4-7.

---

## Що тобі потрібно

| Інструмент             | Версія             | Перевірка                          |
|------------------------|--------------------|------------------------------------|
| **JDK**                | 24+                | `java -version`                    |
| **IntelliJ IDEA**      | будь-яка свіжа     | відкрити проект                    |
| **curl** (або Postman) | будь-яка           | `curl --version`                   |
| **jq** (опціонально)   | для форматування JSON | `jq --version` (mac: `brew install jq`) |
| **Docker** (опціонально) | для Практики 10 (Kafka) | `docker --version`               |
| **grpcurl** (опціонально) | для Практики 9 (gRPC) | `brew install grpcurl`           |

> Docker і grpcurl потрібні **тільки** для конкретних розділів. Без них перші 8
> практик усе одно працюють.

---

## Запуск проекту

У терміналі з папки проекту:

```bash
./gradlew bootRun
```

Перший запуск довгий (Gradle качає залежності, ~3-5 хв).

Коли побачиш у логах рядок типу
`Started ApiExampleApplication in X seconds` — відкрий у браузері:

http://localhost:8080

Це головна сторінка з посиланнями на всі демо. Якщо вона відкрилась — все добре,
переходь до Практики 1.

> **Зупинити додаток:** `Ctrl+C` у терміналі.

---

## Практика 1 — Дивимось контракт API через Swagger UI

> **Тема:** Слайди 2, 9 — компоненти API, документація.

### Що це?

Будь-який REST API має **контракт**: список endpoints, формати запитів і
відповідей, можливі помилки. Формальний стандарт опису — **OpenAPI**, його
візуальна обгортка — **Swagger UI**. Це місце, де frontend-розробник, QA або
зовнішня команда дивиться "як з тобою інтегруватись".

### Подивись код

- [`config/OpenApiConfig.kt`](src/main/kotlin/this4you/apiexample/config/OpenApiConfig.kt) — мета-інформація API і security schemes.
- [`books/v1/BookDtoV1.kt`](src/main/kotlin/this4you/apiexample/books/v1/BookDtoV1.kt) — звернути увагу на анотації `@Schema(...)`.
- [`books/v1/BookControllerV1.kt`](src/main/kotlin/this4you/apiexample/books/v1/BookControllerV1.kt) — анотації `@Operation`, `@Tag` пояснюють endpoints.

### Спробуй сам

1. Відкрий http://localhost:8080/swagger-ui.html
2. Розгорни розділ **Books v1**. Скільки endpoints бачиш? Які HTTP методи?
3. Розгорни схему `BookRequestV1` внизу сторінки. Знайди:
   - яке поле має `pattern: \d{13}`?
   - яке поле повинно бути `>= 0`?
4. Натисни на будь-який endpoint → "Try it out" → "Execute". Що повернулось?
   (Поки що буде 401 — це нормально, ми ще не залогінились).

### Подумай

Як виглядав би код фронтенду, який знає тільки URL `http://localhost:8080`,
без Swagger? Скільки помилок зробив би розробник, який пише інтеграцію
"навмання"?

---

## Практика 2 — Перший REST запит (GET без auth)

> **Тема:** Слайди 1, 6 — як працює API, RESTful принципи.

### Що це?

REST API — це HTTP-запити з осмисленими URL-ами і методами. `GET /something`
= "дай мені дані". У нас є один публічний endpoint, який не вимагає
аутентифікації, бо він викликає **зовнішній** public API:

### Подивись код

- [`integration/ExternalApiClient.kt`](src/main/kotlin/this4you/apiexample/integration/ExternalApiClient.kt) — наш сервіс викликає `jsonplaceholder.typicode.com`.

Зверни увагу: наш бекенд тут одночасно **сервер** для нас і **клієнт** для
зовнішнього API. Тобто інтеграція між системами — це теж API виклик.

### Спробуй сам

```bash
curl http://localhost:8080/integration/external-posts
```

або з форматуванням:

```bash
curl -s http://localhost:8080/integration/external-posts | jq
```

### Подумай

- Скільки HTTP-запитів зробилось при одному виклику цього endpoint? (Підказка:
  один зовнішній + наш = 2.)
- Що буде, якщо `jsonplaceholder.typicode.com` ляже? Як це вплине на наш API?

---

## Практика 3 — Аутентифікація через JWT

> **Тема:** Слайд 7.

### Що це?

API без захисту = ваші дані доступні всім. У нашому проекті більшість
endpoints вимагають **JWT** (JSON Web Token). Сценарій:

1. Клієнт надсилає логін/пароль на `/auth/login`.
2. Сервер перевіряє і повертає підписаний JWT.
3. У всіх наступних запитах клієнт додає заголовок `Authorization: Bearer <token>`.
4. Сервер перевіряє підпис і знає, хто це.

JWT — **stateless**: сервер не зберігає сесії, всі дані про користувача — у
самому токені (Слайд 6).

### Подивись код

- [`auth/AuthController.kt`](src/main/kotlin/this4you/apiexample/auth/AuthController.kt) — endpoint `/auth/login`.
- [`auth/JwtService.kt`](src/main/kotlin/this4you/apiexample/auth/JwtService.kt) — генерація і верифікація.
- [`auth/JwtAuthFilter.kt`](src/main/kotlin/this4you/apiexample/auth/JwtAuthFilter.kt) — фільтр, що читає заголовок.
- [`config/SecurityConfig.kt`](src/main/kotlin/this4you/apiexample/config/SecurityConfig.kt) — які endpoints захищені.

### Спробуй сам

**Крок 1 — запит без токена → отримуємо 401:**
```bash
curl -i http://localhost:8080/api/v1/books
```

**Крок 2 — логін, отримуємо JWT:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

Скопіюй значення `token` з відповіді. Або зроби це автоматично:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r .token)
echo $TOKEN
```

**Крок 3 — тепер той самий запит, але з токеном:**
```bash
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/books
```

Це має повернути 200 і список книг.

**Крок 4 — інтерактивне демо:**
Відкрий http://localhost:8080/api-demo.html — графічна сторінка з кнопками.

### Облікові дані для цієї практики

| username | password | role  |
|----------|----------|-------|
| admin    | admin    | ADMIN |
| user     | user     | USER  |

(Це навмисно слабкі тестові паролі. У реальному API — BCrypt + БД користувачів + бажано MFA.)

### Подумай

Що буде, якщо змінити одну літеру в токені і надіслати знову? Спробуй:
```bash
BAD_TOKEN="${TOKEN%?}X"   # підмінили останній символ
curl -i -H "Authorization: Bearer $BAD_TOKEN" http://localhost:8080/api/v1/books
```

---

## Практика 4 — REST CRUD (Create, Read, Update, Delete)

> **Тема:** Слайди 6, 10 — RESTful принципи, статус-коди.

### Що це?

CRUD = 4 базові операції з ресурсом. У REST вони мапляться на HTTP методи:

| Дія         | HTTP метод | Очікуваний статус відповіді |
|-------------|------------|------------------------------|
| Створити    | POST       | 201 Created + `Location` header |
| Прочитати   | GET        | 200 OK                       |
| Оновити     | PUT/PATCH  | 200 OK                       |
| Видалити    | DELETE     | 204 No Content               |

URL описує **ресурс** (`/books/5`), а **дію** — HTTP метод. Не треба робити
`/getBooks` або `/deleteBook` — це антипатерн.

### Подивись код

- [`books/v1/BookControllerV1.kt`](src/main/kotlin/this4you/apiexample/books/v1/BookControllerV1.kt) — усі чотири операції.

### Спробуй сам

> Не забудь спочатку отримати `$TOKEN` з Практики 3.

**GET — список:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/books
```

**GET — одна книга:**
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/books/1
```

**POST — створити:**
```bash
curl -i -X POST http://localhost:8080/api/v1/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Designing Data-Intensive Applications",
    "author": "Martin Kleppmann",
    "isbn": "9781449373320",
    "priceUah": 1400
  }'
```
Подивись у заголовках відповіді: який статус і чи є `Location`?

**PUT — оновити:**
```bash
curl -i -X PUT http://localhost:8080/api/v1/books/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Kotlin in Action (Updated)",
    "author": "Dmitry Jemerov",
    "isbn": "9781617293290",
    "priceUah": 999
  }'
```

**DELETE — видалити:**
```bash
curl -i -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/books/3
```
Який статус повернувся?

### Подумай

Чому DELETE повертає 204, а не 200? У чому різниця між `Cache-Control: public`
і `Cache-Control: no-store` (підказка: глянь на GET відповідь)?

---

## Практика 5 — Помилки і трасування

> **Тема:** Слайд 10.

### Що це?

Хороший API повертає **зрозумілі** помилки: правильний HTTP статус + JSON з
кодом, повідомленням, і `traceId` для дебагу. `traceId` — це унікальний
ідентифікатор запиту, він з'являється в кожному рядку логу. Якщо щось
зламалось — користувач каже traceId, ти йдеш у логи і знаходиш весь шлях.

### Подивись код

- [`common/ErrorResponse.kt`](src/main/kotlin/this4you/apiexample/common/ErrorResponse.kt) — структура помилки.
- [`common/GlobalExceptionHandler.kt`](src/main/kotlin/this4you/apiexample/common/GlobalExceptionHandler.kt) — exception → HTTP статус.
- [`common/CorrelationIdFilter.kt`](src/main/kotlin/this4you/apiexample/common/CorrelationIdFilter.kt) — генерація і пропагація traceId.

### Спробуй сам

**404 — ресурс не знайдено:**
```bash
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/books/9999
```

Подивись на тіло відповіді — це не текст, а структурований JSON з полями
`code`, `message`, `traceId`, `timestamp`.

**400 — валідація:**
```bash
curl -i -X POST http://localhost:8080/api/v1/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"","author":"","isbn":"bad","priceUah":-5}'
```
Що в полі `details`?

**Власний traceId** (так робить API Gateway):
```bash
curl -i -H "X-Trace-Id: my-experiment-001" \
     -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/v1/books/9999
```
Подивись на:
- заголовок `X-Trace-Id` у відповіді (наш traceId),
- логи в терміналі — там тепер `[traceId=my-experiment-001]`.

### Подумай

Уяви, що користувач пише в підтримку: "у мене не відкривається сторінка
замовлень". Як ти знайдеш проблему в логах серед мільйонів запитів за день,
якщо в нього є traceId? А якщо немає?

---

## Практика 6 — Версіонування API (v1 vs v2)

> **Тема:** Слайд 8.

### Що це?

API змінюються. Якщо ти зміниш формат відповіді — старі клієнти зламаються.
Тому додаємо нову версію, а стару тримаємо живою, поки клієнти мігрують.
Способи: URI versioning (`/v1/...`, `/v2/...`), header versioning, content
negotiation. Найпопулярніший — URI.

### Подивись код

- [`books/v1/`](src/main/kotlin/this4you/apiexample/books/v1) і [`books/v2/`](src/main/kotlin/this4you/apiexample/books/v2) — два пакети, два контракти.
- [`BookService.kt`](src/main/kotlin/this4you/apiexample/books/BookService.kt) — спільна бізнес-логіка.
- У `BookControllerV1.kt` подивись на додані заголовки `Deprecation`, `Sunset`, `Link`.

### Спробуй сам

**Подивись різницю в контракті:**
```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/books/1 | jq
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v2/books/1 | jq
```

Що змінилось? (Підказка: ціна.)

**Подивись заголовки v1 — там попередження:**
```bash
curl -sI -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/books | grep -iE "deprecation|sunset|link"
```

### Подумай

Якщо ти додаси **нове опціональне поле** в JSON відповіді — це breaking change?
А якщо видалиш існуюче? А якщо зміниш тип з `int` на `string`?

---

## Практика 7 — GraphQL

> **Тема:** Слайди 4, 9.

### Що це?

REST повертає **фіксований** набір полів. GraphQL дозволяє **клієнту** обирати,
які поля йому потрібні. Зручно для мобільних застосунків і складних UI, де
різним екранам потрібні різні дані.

### Подивись код

- [`graphql/schema.graphqls`](src/main/resources/graphql/schema.graphqls) — формальна схема.
- [`graphql/BookGraphQLController.kt`](src/main/kotlin/this4you/apiexample/graphql/BookGraphQLController.kt) — реалізація.

### Спробуй сам

Відкрий http://localhost:8080/graphiql (це інтерактивний UI типу Swagger,
тільки для GraphQL).

**Запит 1 — тільки `title`:**
```graphql
{ books { title } }
```

**Запит 2 — додай ще `author` і `priceUah`:**
```graphql
{ books { title author priceUah } }
```

**Запит 3 — одна книга:**
```graphql
{ book(id: 1) { title author isbn } }
```

**Запит 4 — створи нову через мутацію:**
```graphql
mutation {
  createBook(input: {
    title: "GraphQL in Action",
    author: "Samer Buna",
    isbn: "9781617295683",
    priceUah: 1300
  }) {
    id
    title
  }
}
```

### Подумай

Що ефективніше для **мобільного** застосунку зі слабким інтернетом: REST, який
завжди повертає всі поля, чи GraphQL? А для backend-to-backend, де трафік
безкоштовний?

---

## Практика 8 — WebSocket (real-time чат)

> **Тема:** Слайд 5.

### Що це?

HTTP — це **request/response**: клієнт спитав, сервер відповів, з'єднання
закрилось. Для чату або біржі це погано: треба постійно опитувати сервер.
WebSocket створює **постійне двостороннє** з'єднання — сервер сам "штовхає"
повідомлення клієнту.

### Подивись код

- [`chat/ChatWebSocketHandler.kt`](src/main/kotlin/this4you/apiexample/chat/ChatWebSocketHandler.kt) — обробник повідомлень.
- [`config/WebSocketConfig.kt`](src/main/kotlin/this4you/apiexample/config/WebSocketConfig.kt) — реєстрація endpoint `/ws/chat`.
- [`static/chat.html`](src/main/resources/static/chat.html) — клієнт.

### Спробуй сам

1. Відкрий http://localhost:8080/chat.html **у двох вкладках браузера**.
2. Напиши повідомлення в одній.
3. Бачиш його миттєво в іншій? Так працює broadcast через WebSocket.

### Подумай

Telegram надсилає тобі повідомлення миттєво. Як це реалізовано — твій телефон
кожну секунду питає "є нові повідомлення?", чи Telegram сам надсилає?

---

## Практика 9 — SOAP і gRPC (порівняння з REST)

> **Тема:** Слайд 4.

### Що це?

REST — не єдиний стиль API. У нашому проекті є той самий ресурс (Books)
в **чотирьох** стилях: REST, GraphQL, SOAP, gRPC. SOAP — старий
enterprise-стандарт на XML. gRPC — сучасний бінарний RPC на HTTP/2 від Google.

### SOAP — спробуй сам

**Подивись WSDL** (формальний контракт SOAP):
http://localhost:8080/ws-services/books.wsdl

**Зроби запит:**
```bash
curl -X POST http://localhost:8080/ws-services \
  -H 'Content-Type: text/xml' \
  --data '<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:b="http://this4you/apiexample/soap/books">
    <soap:Body>
      <b:GetBookRequest><b:id>1</b:id></b:GetBookRequest>
    </soap:Body>
  </soap:Envelope>'
```

Файли: [`soap/BookSoapEndpoint.kt`](src/main/kotlin/this4you/apiexample/soap/BookSoapEndpoint.kt), [`xsd/books.xsd`](src/main/resources/xsd/books.xsd).

### gRPC — спробуй сам

(Потрібен `grpcurl`. Без нього просто подивись код.)

```bash
grpcurl -plaintext -d '{}' localhost:9090 book.BookGrpcApi/List
grpcurl -plaintext -d '{"id":1}' localhost:9090 book.BookGrpcApi/Get
```

Файли: [`proto/book.proto`](src/main/proto/book.proto), [`grpc/BookGrpcService.kt`](src/main/kotlin/this4you/apiexample/grpc/BookGrpcService.kt).

### Подумай

Порівняй розмір відповіді у трьох форматах:
```bash
# REST JSON
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/books/1 | wc -c
# SOAP XML
curl -s -X POST http://localhost:8080/ws-services -H 'Content-Type: text/xml' \
  --data '<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:b="http://this4you/apiexample/soap/books"><soap:Body><b:GetBookRequest><b:id>1</b:id></b:GetBookRequest></soap:Body></soap:Envelope>' | wc -c
```
Який формат компактніший? А якщо би в нас було gRPC (бінарне) — як думаєш?

---

## Практика 10 — Асинхронні інтеграції з Kafka

> **Тема:** Слайд 11. Потрібен Docker.

### Що це?

Іноді відповідь користувачу треба дати **зараз**, але обробити подію — пізніше,
бо обробка повільна або задіює багато сервісів. Тоді ми не викликаємо їх
прямо, а кидаємо **подію** в брокер повідомлень (Kafka). Інші сервіси
підписуються на цей топік і реагують у своєму темпі.

### Подивись код

- [`messaging/KafkaProducer.kt`](src/main/kotlin/this4you/apiexample/messaging/KafkaProducer.kt) — публікація.
- [`messaging/KafkaConsumer.kt`](src/main/kotlin/this4you/apiexample/messaging/KafkaConsumer.kt) — підписка.
- [`webhooks/WebhookSenderService.kt`](src/main/kotlin/this4you/apiexample/webhooks/WebhookSenderService.kt) — інший спосіб (HTTP callback).
- [`orders/OrderService.kt`](src/main/kotlin/this4you/apiexample/orders/OrderService.kt) — оркеструє все це.

### Спробуй сам

**Крок 1 — підняти Kafka і Kafka UI:**
```bash
docker compose up -d
```
Зачекай хвилину, поки контейнери стартують.

**Крок 2 — перезапусти Spring Boot додаток** (`Ctrl+C` + `./gradlew bootRun`), щоб він підключився до Kafka.

**Крок 3 — відкрий Kafka UI:** http://localhost:8090
Поки що топік `order.created` не існує.

**Крок 4 — створи замовлення:**
```bash
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"quantity":2}'
```

**Крок 5 — подивись що сталось:**
- У логах Spring Boot: послідовність `OrderService` → `KafkaProducer` → `KafkaConsumer` → `WebhookSenderService` → `WebhookReceiverController`.
- У Kafka UI: топік `order.created` з'явився, всередині — твоє повідомлення.
- У розділі **Consumers** — група `api-example` з offset-ом.

### Подумай

Який сценарій кращий для оплати замовлення:
- (a) фронтенд чекає 30 секунд, поки банк підтвердить операцію,
- (b) бекенд приймає замовлення, повертає 202 Accepted, а далі обробляє асинхронно через чергу і повідомляє webhook-ом, коли все готово?

---

## Практика 11 — Rate Limiting (захист API)

> **Тема:** Слайди 7, 12.

### Що це?

Якщо хтось почне зловживати API (наприклад, brute-force паролів), він покладе
сервер. Тому ми обмежуємо кількість запитів від одного клієнта (IP, ключа,
користувача).

### Подивись код

- [`common/RateLimitFilter.kt`](src/main/kotlin/this4you/apiexample/common/RateLimitFilter.kt) — реалізація через Bucket4j.
- [`application.yml`](src/main/resources/application.yml) — параметр `demo.rate-limit.requests-per-minute`.

### Спробуй сам

1. Відкрий `application.yml` і зміни `requests-per-minute: 30` на **5**.
2. Перезапусти додаток.
3. Виконай:
   ```bash
   for i in {1..10}; do
     curl -s -o /dev/null -w "запит $i → %{http_code}\n" \
       http://localhost:8080/integration/external-posts
   done
   ```
4. Після 5-го запиту повертається `429 Too Many Requests`.
5. Поверни значення на 30 і перезапусти.

### Подумай

API Gateway (Kong, AWS API Gateway) робить це у production. Чому це краще
робити **до** того, як запит дійде до твого мікросервісу?

---

## Практика 12 — Моніторинг (Actuator)

> **Тема:** Слайд 13.

### Що це?

API без моніторингу = ти не знаєш, що зламалось, поки не подзвонять
користувачі. Spring Boot Actuator дає готові endpoints для health checks і
метрик.

### Спробуй сам

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
curl http://localhost:8080/actuator/prometheus
```

Останній — формат, який збирає **Prometheus**, а потім **Grafana** малює
графіки. Це стандарт індустрії.

### Подумай

Які 5 метрик ти би хотів бачити на дашборді production API? (Підказка:
latency, error rate, RPS, ...)

---

## Чекліст пройденого

Постав галочку, коли пройдеш:

- [ ] Практика 1 — Swagger UI
- [ ] Практика 2 — GET без auth
- [ ] Практика 3 — JWT login + захищений запит
- [ ] Практика 4 — REST CRUD
- [ ] Практика 5 — помилки + traceId
- [ ] Практика 6 — v1 vs v2
- [ ] Практика 7 — GraphQL
- [ ] Практика 8 — WebSocket
- [ ] Практика 9 — SOAP + gRPC
- [ ] Практика 10 — Kafka (з Docker)
- [ ] Практика 11 — Rate limiting
- [ ] Практика 12 — Actuator

---

## Що далі?

Якщо все це зрозуміло і легко — ось ідеї для розширення:

- Додай `PATCH` метод у `BookControllerV1` — часткове оновлення.
- Зроби свій webhook receiver, який пише в окремий лог-файл.
- Додай ще одну роль (`MANAGER`) і ендпоінт, доступний тільки їй.
- Зашифруй секрет JWT через env variable замість хардкоду в `application.yml`.
- Запиши власне інтеграційне тестування для `OrderController` (приклад є у `src/test/`).

---

## Структура проекту (швидка довідка)

```
src/main/kotlin/this4you/apiexample/
├── ApiExampleApplication.kt       — точка входу
├── auth/                          — JWT, API Key, login (Практика 3)
├── books/                         — REST CRUD + v1/v2 (Практики 4, 6)
│   ├── v1/                        — стара версія API
│   └── v2/                        — нова версія API
├── orders/                        — приклад event-driven (Практика 10)
├── webhooks/                      — webhook send + receive (Практика 10)
├── messaging/                     — Kafka (Практика 10)
├── chat/                          — WebSocket (Практика 8)
├── graphql/                       — GraphQL (Практика 7)
├── grpc/                          — gRPC (Практика 9)
├── soap/                          — SOAP (Практика 9)
├── integration/                   — клієнт public API (Практика 2)
├── async/                         — async endpoint
├── common/                        — фільтри, помилки (Практика 5, 11)
└── config/                        — Security, Swagger, WebSocket
src/main/resources/
├── application.yml                — конфігурація
├── graphql/schema.graphqls        — GraphQL schema
├── xsd/books.xsd                  — SOAP контракт
├── static/                        — HTML-сторінки демо
└── http/                          — .http файли для IntelliJ REST Client
src/main/proto/book.proto          — gRPC контракт
docker-compose.yml                 — Kafka + Kafka UI
docs/
├── lecture.md                     — повний текст лекції
└── use-cases.md                   — мапа "слайд → код" для викладача
```

---

## Корисні посилання

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **GraphiQL:** http://localhost:8080/graphiql
- **Kafka UI:** http://localhost:8090 (потребує `docker compose up -d`)
- **Actuator:** http://localhost:8080/actuator/health
- **WSDL (SOAP):** http://localhost:8080/ws-services/books.wsdl
- **Головна:** http://localhost:8080/

Питання? Підіймай руку, кричи, пиши в чат — викладач тут саме для цього.
