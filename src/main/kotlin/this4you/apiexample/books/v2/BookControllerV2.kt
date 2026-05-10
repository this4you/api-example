package this4you.apiexample.books.v2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import this4you.apiexample.books.BookService

// === Слайд 8 — v2 версія API для книг ===
// Той самий ресурс /books, але новий контракт (DTO).
// Сервіс шар (BookService) спільний — повторне використання логіки.
@RestController
@RequestMapping("/api/v2/books")
@Tag(name = "Books v2", description = "Слайд 8 — нова версія API з валютою у структурованому полі")
@SecurityRequirement(name = "bearerAuth")
class BookControllerV2(private val bookService: BookService) {

    @GetMapping
    @Operation(summary = "Список книг (v2)")
    fun list(): List<BookResponseV2> =
        bookService.list().map { BookResponseV2.from(it) }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): BookResponseV2 =
        BookResponseV2.from(bookService.get(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: BookRequestV2): BookResponseV2 {
        val created = bookService.create(
            title = request.title,
            author = request.author,
            isbn = request.isbn,
            priceUah = request.priceAmount  // у демо приймаємо тільки UAH
        )
        return BookResponseV2.from(created)
    }
}
