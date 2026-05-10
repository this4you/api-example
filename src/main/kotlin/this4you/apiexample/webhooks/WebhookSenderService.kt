package this4you.apiexample.webhooks

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import this4you.apiexample.orders.Order

// === Слайд 11 — Webhook sender ===
// Webhook — це "зворотний виклик": ми (як партнерська система) повідомляємо
// інший сервіс про подію через HTTP POST. Він не опитує нас постійно,
// а отримує push-нотифікацію.
// Так працюють Stripe (оплата), GitHub (push), Telegram (callback).
//
// @Async — викликається у фоні, щоб користувач не чекав на повільний webhook.
@Service
class WebhookSenderService(
    @Value("\${demo.webhook.target-url}") private val targetUrl: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val client = RestClient.create()

    @Async
    fun notifyOrderCreated(order: Order) {
        log.info("Sending webhook for order {}", order.id)
        try {
            client.post()
                .uri(targetUrl)
                // Webhook зазвичай має підпис, щоб одержувач міг перевірити справжність.
                // У реальному API: HMAC-SHA256 від тіла з shared secret.
                .header("X-Webhook-Signature", "demo-signature-${order.id.take(8)}")
                .header("Content-Type", "application/json")
                .body(
                    """{"event":"order.created","orderId":"${order.id}","bookId":${order.bookId}}"""
                )
                .retrieve()
                .toBodilessEntity()
            log.info("Webhook delivered for order {}", order.id)
        } catch (ex: Exception) {
            // Слайд 11 — webhook може провалитись. У production:
            //   - retry з exponential backoff (Слайд 10),
            //   - dead-letter queue,
            //   - моніторинг доставки.
            log.warn("Webhook failed for order ${order.id}: ${ex.message}")
        }
    }
}
