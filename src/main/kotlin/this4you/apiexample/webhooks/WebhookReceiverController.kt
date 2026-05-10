package this4you.apiexample.webhooks

import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// === Слайд 11 — Webhook receiver ===
// "Інша сторона" — endpoint, який приймає вхідні webhook повідомлення.
// Наприклад, Stripe викликає його після успішної оплати.
// У реальному API:
//   1) перевірити підпис (HMAC) — щоб уникнути фейкових повідомлень,
//   2) повернути 200 швидко — інакше відправник почне retry,
//   3) обробити подію асинхронно (через чергу або фоновий job).
@RestController
@RequestMapping("/webhooks")
@Tag(name = "Webhooks", description = "Слайд 11 — приклад прийому webhook")
class WebhookReceiverController {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/received")
    fun receive(
        @RequestHeader(value = "X-Webhook-Signature", required = false) signature: String?,
        @RequestBody payload: String
    ): Map<String, String> {
        log.info("Received webhook: signature={}, payload={}", signature, payload)
        // Тут б ми перевіряли підпис, але це демо.
        return mapOf("status" to "received")
    }
}
