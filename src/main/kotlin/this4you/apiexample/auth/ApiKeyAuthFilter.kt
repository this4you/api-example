package this4you.apiexample.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// === Слайд 7 — Authentication через API key ===
// Альтернатива JWT для machine-to-machine інтеграцій (Слайд 3 — Partner API).
// Клієнт надсилає заголовок: X-API-Key: demo-api-key-12345
// У реальній системі ключі зберігаються в базі і мають метадані:
//   - кому належать (партнер X),
//   - які scope-и дозволені,
//   - дата expiration,
//   - чи можна відкликати (revoke).
@Component
class ApiKeyAuthFilter(
    @Value("\${demo.api-key.valid-keys}") private val validKeys: List<String>
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val apiKey = request.getHeader("X-API-Key")
        if (apiKey != null && apiKey in validKeys) {
            val auth = UsernamePasswordAuthenticationToken(
                "api-client",
                null,
                listOf(SimpleGrantedAuthority("ROLE_PARTNER"))
            )
            SecurityContextHolder.getContext().authentication = auth
        }
        filterChain.doFilter(request, response)
    }
}
