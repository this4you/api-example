package this4you.apiexample.common

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

// === Слайд 10 — Кореляція запитів (traceId / correlationId) ===
// У мікросервісах один запит проходить через багато сервісів.
// Щоб знайти весь ланцюжок у логах — додаємо заголовок X-Trace-Id.
//
// Цей фільтр:
//   1) бере traceId з заголовка (якщо вже існує — наприклад, прийшло з API Gateway),
//   2) інакше генерує новий UUID,
//   3) кладе його в MDC, щоб він автоматично з'являвся у логах (див. application.yml),
//   4) повертає його в response header, щоб клієнт міг показати у звіті про помилку.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // має бути першим — щоб traceId був у логах одразу
class CorrelationIdFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val traceId = request.getHeader(HEADER) ?: UUID.randomUUID().toString()
        MDC.put("traceId", traceId)
        response.setHeader(HEADER, traceId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("traceId")  // важливо очистити: thread reused
        }
    }

    companion object {
        const val HEADER = "X-Trace-Id"
    }
}
