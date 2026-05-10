package this4you.apiexample.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// === Слайд 7 — Endpoint для логіну ===
// Дуже спрощена реалізація: у реальному API ми перевіряли б користувача в БД,
// порівнювали б hash паролів (BCrypt), можливо викликали б OAuth2 сервер.
// Тут — два хардкодовані демо-користувачі.
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Слайд 7 — аутентифікація та видача JWT")
class AuthController(private val jwtService: JwtService) {

    @PostMapping("/login")
    @Operation(summary = "Видає JWT за пару логін/пароль (Слайд 7)")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        // У реальному коді — userRepo.findByUsername + BCrypt.matches
        val (role, ok) = when {
            request.username == "admin" && request.password == "admin" -> "ADMIN" to true
            request.username == "user"  && request.password == "user"  -> "USER"  to true
            else -> "" to false
        }
        if (!ok) {
            // 401 Unauthorized — Слайд 10
            return ResponseEntity.status(401).build()
        }
        val token = jwtService.generate(request.username, role)
        return ResponseEntity.ok(LoginResponse(token = token, role = role))
    }
}

data class LoginRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)

data class LoginResponse(val token: String, val role: String)
