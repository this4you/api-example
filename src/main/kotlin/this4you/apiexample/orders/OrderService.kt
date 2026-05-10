package this4you.apiexample.orders

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import this4you.apiexample.books.BookService
import this4you.apiexample.messaging.KafkaProducer
import this4you.apiexample.webhooks.WebhookSenderService
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// === Слайди 10, 11 — Order Service ===
// Створення замовлення демонструє event-driven підхід:
//   1) синхронно: переконатись що книга існує, зберегти order,
//   2) асинхронно: опублікувати подію в Kafka,
//      викликати webhook зовнішньої системи.
// Це класичний приклад mix-у синхронної відповіді клієнту з фоновою обробкою.
@Service
class OrderService(
    private val bookService: BookService,
    private val kafkaProducer: KafkaProducer,
    private val webhookSender: WebhookSenderService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val store = ConcurrentHashMap<String, Order>()

    fun create(bookId: Long, quantity: Int): Order {
        bookService.get(bookId)  // кидає 404 якщо книги немає (Слайд 10)

        val order = Order(
            id = UUID.randomUUID().toString(),
            bookId = bookId,
            quantity = quantity,
            status = Order.Status.PENDING
        )
        store[order.id] = order
        log.info("Order created: id={}, bookId={}", order.id, order.bookId)

        // === Слайд 11 — асинхронні інтеграції ===
        // Подія "OrderCreated" у Kafka — інші сервіси (notification, analytics,
        // warehouse) отримають її незалежно один від одного (Pub/Sub).
        val payload = """{"orderId":"${order.id}","bookId":${order.bookId},"qty":${order.quantity}}"""
        kafkaProducer.publishOrderCreated(payload)
        // Webhook — повідомлення зовнішнього сервісу через HTTP callback
        webhookSender.notifyOrderCreated(order)

        return order
    }

    fun get(id: String): Order? = store[id]
}
