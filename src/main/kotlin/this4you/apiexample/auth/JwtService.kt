package this4you.apiexample.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

// === Слайд 7 — JWT (JSON Web Token) ===
// JWT — це самодостатній токен, що містить:
//   header (алгоритм підпису),
//   payload (claims: userId, role, exp...),
//   signature (підпис, щоб не можна було підробити).
//
// Сервер підписує токен своїм секретом і не зберігає сесії —
// це робить API stateless (Слайд 6).
@Service
class JwtService(
    @Value("\${demo.jwt.secret}") private val secret: String,
    @Value("\${demo.jwt.expiration-minutes}") private val expirationMinutes: Long
) {
    // Секрет повинен бути >= 256 біт для HS256
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    // Генерація токена після успішного логіну
    fun generate(username: String, role: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMinutes * 60_000)
        return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)  // Слайд 7 — токен має обмежений час життя
            .signWith(key)
            .compact()
    }

    // Перевірка підпису + парсинг claims. Кидає виключення, якщо токен невалідний.
    fun parse(token: String): JwtClaims {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
        return JwtClaims(
            username = claims.subject,
            role = claims["role"] as? String ?: "USER"
        )
    }
}

data class JwtClaims(val username: String, val role: String)
