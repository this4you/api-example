package this4you.apiexample.books.v2

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import this4you.apiexample.books.Book

// === Слайд 8 — v2 контракт ===
// У v2 ми:
//   - перейменували priceUah → price (об'єкт з валютою), це BREAKING change,
//     тому це окрема версія, а не патч до v1,
//   - додали поле tags (нове поле — це backward-compatible, було б ок і в v1,
//     якби клієнти ігнорували невідомі поля).
//
// Це типовий приклад того, чому потрібне версіонування (Слайд 8).

@Schema(description = "Запит на створення книги (v2)")
data class BookRequestV2(
    @field:NotBlank val title: String,
    @field:NotBlank val author: String,
    @field:Pattern(regexp = "\\d{13}") val isbn: String,
    @field:Min(0) val priceAmount: Int,
    val priceCurrency: String = "UAH",
    val tags: List<String> = emptyList()
)

@Schema(description = "Книга (v2 контракт)")
data class BookResponseV2(
    val id: Long,
    val title: String,
    val author: String,
    val isbn: String,
    val price: Price,
    val tags: List<String> = emptyList()
) {
    data class Price(val amount: Int, val currency: String)

    companion object {
        fun from(book: Book) = BookResponseV2(
            id = book.id,
            title = book.title,
            author = book.author,
            isbn = book.isbn,
            price = Price(book.priceUah, "UAH"),
            tags = emptyList()
        )
    }
}
