package this4you.apiexample.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import io.jsonwebtoken.JwtException

// === Слайд 13 — Unit тест без Spring контексту ===
// Швидкий, ізольований — викликає тільки JwtService.
class JwtServiceTest {

    private val service = JwtService(
        secret = "test-secret-key-must-be-at-least-32-bytes-long-for-hs256-yes",
        expirationMinutes = 60
    )

    @Test
    fun `generate and parse round-trip`() {
        val token = service.generate("alice", "USER")
        val claims = service.parse(token)
        assertEquals("alice", claims.username)
        assertEquals("USER", claims.role)
    }

    @Test
    fun `parsing garbage throws`() {
        assertFailsWith<JwtException> {
            service.parse("not-a-jwt")
        }
    }
}
