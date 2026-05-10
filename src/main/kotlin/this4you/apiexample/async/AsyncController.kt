package this4you.apiexample.async

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

// === Слайд 11 — Асинхронні endpoints ===
// Іноді операція повільна (генерація звіту, обробка платежу).
// Замість того, щоб тримати з'єднання відкритим хвилинами, ми:
//   - або повертаємо 202 Accepted одразу і завершуємо у фоні,
//   - або повертаємо CompletableFuture (HTTP-з'єднання тримаємо, але потік не блокуємо).
@RestController
@RequestMapping("/api/public/async")
@Tag(name = "Async", description = "Слайд 11 — асинхронні запити")
class AsyncController(private val slow: SlowService) {

    @GetMapping("/report")
    @Operation(summary = "Імітує повільну операцію (1 сек) — повертає CompletableFuture")
    fun report(): CompletableFuture<Map<String, String>> = slow.generateReport()
}

@Service
class SlowService {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async  // Виконується в окремому пулі потоків (Слайд 11)
    fun generateReport(): CompletableFuture<Map<String, String>> {
        log.info("Generating report (slow operation)...")
        Thread.sleep(1000)
        return CompletableFuture.completedFuture(mapOf("report" to "ready", "duration" to "1000ms"))
    }
}
