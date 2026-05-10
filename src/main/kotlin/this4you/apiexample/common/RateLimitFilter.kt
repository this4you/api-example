package this4you.apiexample.common

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

// === Слайди 7, 12 — Rate limiting ===
// API Gateway або сам сервіс обмежує кількість запитів від клієнта,
// щоб захистити систему від перевантаження або brute-force атак.
//
// Це найпростіша реалізація: окремий "bucket" токенів на кожний IP.
// Алгоритм Token Bucket: ми витрачаємо по 1 токену з кожним запитом,
// токени поповнюються із заданою швидкістю.
@Component
class RateLimitFilter(
    @Value("\${demo.rate-limit.requests-per-minute}") private val limit: Long
) : OncePerRequestFilter() {

    // Ключ = IP клієнта. У реальній системі — користувач + API ключ.
    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Не лімітуємо службові endpoints — інакше Swagger UI ламається на демо
        if (request.requestURI.startsWith("/swagger") ||
            request.requestURI.startsWith("/api-docs") ||
            request.requestURI.startsWith("/actuator") ||
            request.requestURI.startsWith("/graphiql")
        ) {
            filterChain.doFilter(request, response)
            return
        }

        val bucket = buckets.computeIfAbsent(request.remoteAddr) { newBucket() }
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response)
        } else {
            // 429 Too Many Requests — стандартний код для rate limit (RFC 6585)
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write(
                """{"code":"RATE_LIMIT","message":"Too many requests, try later"}"""
            )
        }
    }

    private fun newBucket(): Bucket =
        Bucket.builder()
            .addLimit(Bandwidth.builder().capacity(limit).refillGreedy(limit, Duration.ofMinutes(1)).build())
            .build()
}
