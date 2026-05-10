package this4you.apiexample.orders

import java.time.Instant

// === Доменна модель замовлення ===
data class Order(
    val id: String,
    val bookId: Long,
    val quantity: Int,
    val status: Status,
    val createdAt: Instant = Instant.now()
) {
    enum class Status { PENDING, PAID, FAILED }
}
