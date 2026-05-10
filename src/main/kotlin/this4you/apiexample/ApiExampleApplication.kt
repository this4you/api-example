package this4you.apiexample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

// === Головний клас застосунку ===
// @EnableAsync — Слайд 11: дозволяє запускати методи з @Async окремо від основного потоку.
// Kafka і RabbitMQ автоконфігуруються самостійно через відповідні starter-залежності.
@SpringBootApplication
@EnableAsync
class ApiExampleApplication

fun main(args: Array<String>) {
    runApplication<ApiExampleApplication>(*args)
}
