package this4you.apiexample.books.v1

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import this4you.apiexample.books.Book

// === Слайд 2 — DTO + Bean Validation ===
// DTO відокремлює внутрішню модель (Book) від API контракту.
// Завдяки цьому ми можемо змінити Book у базі, але не зламати клієнтів v1.
// Анотації @NotBlank/@Pattern/@Min — це валідація схеми (Слайд 2).

// === Слайд 8 — Versioning через URI ===
// Це версія v1 DTO. У v2 (див. v2/BookDtoV2.kt) ми додали категорію
// і змінили структуру ціни — це breaking change → нова версія.

@Schema(description = "Запит на створення/оновлення книги (v1)")
data class BookRequestV1(
    @field:NotBlank
    @Schema(example = "Kotlin in Action")
    val title: String,

    @field:NotBlank
    @Schema(example = "Dmitry Jemerov")
    val author: String,

    @field:Pattern(regexp = "\\d{13}", message = "ISBN must be 13 digits")
    @Schema(example = "9781617293290")
    val isbn: String,

    @field:Min(0)
    @Schema(example = "850")
    val priceUah: Int
)

@Schema(description = "Книга (v1 контракт)")
data class BookResponseV1(
    val id: Long,
    val title: String,
    val author: String,
    val isbn: String,
    val priceUah: Int
) {
    companion object {
        fun from(book: Book) = BookResponseV1(
            id = book.id,
            title = book.title,
            author = book.author,
            isbn = book.isbn,
            priceUah = book.priceUah
        )
    }
}
