package this4you.apiexample.messaging

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

// === Слайд 11 — Kafka Consumer ===
// Підписка на топік. Один із багатьох можливих споживачів:
// уявіть NotificationService, AnalyticsService, WarehouseService — кожен
// читає той самий топік для своїх цілей. Це і є Pub/Sub.
@Component
class KafkaConsumer {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaProducer.TOPIC], groupId = "api-example")
    fun onOrderCreated(payload: String) {
        log.info("Kafka consume ← topic={}, payload={}", KafkaProducer.TOPIC, payload)
        // У реальному сервісі тут була б обробка: розсилка email, аналітика тощо.
    }
}
