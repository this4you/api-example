package this4you.apiexample

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

// Простий smoke-тест: контекст підіймається без помилок.
// Вимикаємо gRPC і брокери, щоб не залежати від мережі/портів.
@SpringBootTest
@TestPropertySource(properties = [
    "demo.grpc.enabled=false",
    "spring.kafka.listener.auto-startup=false"
])
class ApiExampleApplicationTests {

    @Test
    fun contextLoads() {
    }
}
