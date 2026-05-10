package this4you.apiexample.orders

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import this4you.apiexample.common.ResourceNotFoundException

// === Слайди 6, 10, 11 — Orders REST API ===
@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Слайд 11 — приклад event-driven обробки замовлень")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // Слайд 10 — 201 для створення
    @Operation(summary = "Створити замовлення (запускає Kafka, Rabbit, Webhook)")
    fun create(@RequestBody request: CreateOrderRequest): Order =
        orderService.create(request.bookId, request.quantity)

    @GetMapping("/{id}")
    @Operation(summary = "Отримати замовлення")
    fun get(@PathVariable id: String): Order =
        orderService.get(id)
            ?: throw ResourceNotFoundException("Order $id not found")
}

data class CreateOrderRequest(
    @field:NotNull val bookId: Long,
    @field:Min(1) val quantity: Int = 1
)
