package this4you.apiexample.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// === Слайд 7 — Перевірка JWT на кожному запиті ===
// Клієнт надсилає токен у заголовку: Authorization: Bearer <jwt>
// Фільтр витягує токен, перевіряє підпис і кладе користувача в SecurityContext.
// Якщо токена немає — пропускаємо: пізніше Spring Security сам поверне 401/403
// для захищених endpoints.
@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ")
            try {
                val claims = jwtService.parse(token)
                val auth = UsernamePasswordAuthenticationToken(
                    claims.username,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_${claims.role}"))
                )
                SecurityContextHolder.getContext().authentication = auth
            } catch (ex: Exception) {
                // Невалідний токен — просто не аутентифікуємо. SecurityFilterChain поверне 401.
                logger.debug("Invalid JWT: ${ex.message}")
            }
        }
        filterChain.doFilter(request, response)
    }
}
