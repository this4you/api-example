package this4you.apiexample.books

// === Доменна модель ===
// Простий immutable data class — як в реальному Kotlin-коді.
data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val isbn: String,
    val priceUah: Int
)
