package this4you.apiexample.books

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

// === Слайд 13 — Інтеграційний тест REST endpoint ===
// @SpringBootTest піднімає весь контекст (як у production).
// @AutoConfigureMockMvc дає MockMvc — fake клієнт для HTTP запитів без сокетів.
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = [
    "demo.grpc.enabled=false",
    "spring.kafka.listener.auto-startup=false"
])
class BookControllerV1Test {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `list books returns 401 without JWT`() {
        // Слайд 7 — без авторизації endpoint захищений
        mockMvc.perform(get("/api/v1/books"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `404 returns structured ErrorResponse`() {
        // Слайд 10 — структуровані помилки
        mockMvc.perform(get("/api/v1/books/9999").header("Authorization", "Bearer " + validJwt()))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("NOT_FOUND"))
            .andExpect(jsonPath("$.traceId").exists())
    }

    private fun validJwt(): String {
        // Інлайн логін, бо в тестах нема готового client helper-а
        val response = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/auth/login")
                .contentType("application/json")
                .content("""{"username":"admin","password":"admin"}""")
        ).andReturn().response.contentAsString
        return Regex("\"token\":\"([^\"]+)\"").find(response)!!.groupValues[1]
    }
}
