package this4you.apiexample.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import this4you.apiexample.auth.ApiKeyAuthFilter
import this4you.apiexample.auth.JwtAuthFilter

// === Слайди 3, 7 — Конфігурація безпеки ===
// Тут визначаємо, які endpoints публічні, а які потребують аутентифікації.
// Spring Security працює як ланцюжок фільтрів (filter chain).
@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val apiKeyAuthFilter: ApiKeyAuthFilter,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Слайд 6 — REST API має бути stateless: токен у кожному запиті,
            // ніяких сесій на сервері.
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // CSRF не потрібен для stateless REST API з токенами
            .csrf { it.disable() }
            // Для простоти демо — basic auth теж дозволений (Слайд 7)
            .httpBasic { }
            .authorizeHttpRequests { auth ->
                auth
                    // === Слайд 3 — Публічні endpoints ===
                    .requestMatchers(
                        "/",
                        "/index.html", "/chat.html", "/api-demo.html",
                        "/auth/**",
                        "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**",
                        "/actuator/**",
                        "/graphiql", "/graphql",
                        "/webhooks/**",       // зовнішні системи надсилають callback
                        "/ws/**",             // WebSocket (Слайд 5)
                        "/api/public/**",     // Слайд 3 — public API
                        "/ws-services/**",    // SOAP endpoint (Слайд 4)
                        "/integration/**"     // приклад public API (Слайд 1)
                    ).permitAll()
                    // === Слайд 3 — Partner API: лише з API ключем ===
                    .requestMatchers("/api/partner/**").hasRole("PARTNER")
                    // === Слайд 7 — Захищені endpoints вимагають JWT ===
                    .requestMatchers("/api/v1/**", "/api/v2/**", "/orders/**").authenticated()
                    .anyRequest().permitAll()
            }
            // Спочатку API-key filter (для partner endpoints), потім JWT
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthFilter,    UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
