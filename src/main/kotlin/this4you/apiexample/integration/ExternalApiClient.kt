package this4you.apiexample.integration

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient

// === Слайди 1, 3 — Інтеграція з public API ===
// Демонструє, як власне API може бути клієнтом іншого API.
// Тут ми звертаємось до публічного API jsonplaceholder.typicode.com,
// який імітує REST бекенд (популярний для навчання).
//
// У реальних проектах це може бути:
//   Google Maps API, OpenWeather API, Stripe API, NBU API курсів валют.
@RestController
@RequestMapping("/integration")
@Tag(name = "Integration", description = "Слайди 1, 3 — інтеграція з зовнішнім public API")
class ExternalApiClient {
    // RestClient — сучасний клієнт у Spring 6+, прийшов на заміну RestTemplate
    private val client: RestClient = RestClient.builder()
        .baseUrl("https://jsonplaceholder.typicode.com")
        .build()

    @GetMapping("/external-posts")
    @Operation(summary = "Викликає публічний API і повертає його дані")
    fun fetchExternalPosts(): List<Map<String, Any>> {
        // Простий GET — як з мобільного або frontend застосунку
        return client.get()
            .uri("/posts?_limit=3")
            .retrieve()
            .body(List::class.java) as List<Map<String, Any>>
    }
}
