package this4you.apiexample.messaging

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

// === Слайд 11 — Kafka Producer ===
// Apache Kafka — це event streaming платформа.
// Producer публікує події в топік. Багато consumer-ів можуть незалежно читати їх.
// Це основа Pub/Sub патерну і event-driven архітектур.
@Component
class KafkaProducer(private val kafka: KafkaTemplate<String, String>) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publishOrderCreated(payload: String) {
        log.info("Kafka publish → topic=order.created, payload={}", payload)
        // Якщо Kafka не запущена — лог буде помилку, але сервіс не падає
        // (missing-topics-fatal: false в application.yml).
        kafka.send(TOPIC, payload)
    }

    companion object {
        const val TOPIC = "order.created"
    }
}
