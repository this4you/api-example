package this4you.apiexample.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// === Слайд 9 — OpenAPI / Swagger UI ===
// Описує API машинно-читабельним способом, з нього автогенерується:
//   - інтерактивна документація (Swagger UI),
//   - клієнтські SDK (Java, Kotlin, TS),
//   - тести контрактів (contract testing).
//
// Swagger UI доступний за адресою: http://localhost:8080/swagger-ui.html
// JSON специфікація: http://localhost:8080/api-docs
@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("API Example — демо до лекції")
                .version("1.0")
                .description(
                    """
                    Цей API демонструє концепції з лекції:
                    REST (Слайди 4, 6), JWT/ApiKey (Слайд 7), Versioning (Слайд 8),
                    Errors (Слайд 10), Webhooks/Async (Слайд 11), Gateway-патерни (Слайд 12).
                    """.trimIndent()
                )
        )
        // Слайд 7 — описуємо схеми авторизації, щоб Swagger UI показав кнопку Authorize
        .components(
            Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
                .addSecuritySchemes(
                    "apiKey",
                    SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .`in`(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                )
        )
}
