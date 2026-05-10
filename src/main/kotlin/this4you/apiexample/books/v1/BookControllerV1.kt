package this4you.apiexample.books.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import this4you.apiexample.books.BookService
import java.time.Duration
import java.time.LocalDate

// === Слайди 4, 6, 8 — REST API v1 для книг ===
// Демонструє основні RESTful принципи:
//   - Ресурс /api/v1/books, ID у URL (Слайд 6)
//   - HTTP методи описують дію: GET/POST/PUT/DELETE (Слайд 6)
//   - Правильні статус-коди: 200, 201, 204, 404 (Слайд 10)
//   - Cache-Control для GET (Слайд 6 — кешування)
//   - Deprecation header — попередження про перехід на v2 (Слайд 8)
@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books v1", description = "Слайд 8 — стара версія API, рекомендована міграція на v2")
@SecurityRequirement(name = "bearerAuth")  // Swagger UI покаже "lock" — потрібен JWT
class BookControllerV1(private val bookService: BookService) {

    @GetMapping
    @Operation(summary = "Список книг (Слайди 4, 6)")
    fun list(): ResponseEntity<List<BookResponseV1>> {
        val body = bookService.list().map { BookResponseV1.from(it) }
        return ResponseEntity.ok()
            // Слайд 6 — кешування: клієнт може зберігати відповідь 30 секунд
            .cacheControl(CacheControl.maxAge(Duration.ofSeconds(30)).cachePublic())
            // Слайд 8 — попередження, що v1 застаріла (RFC 8594)
            .header("Deprecation", "true")
            .header("Sunset", LocalDate.of(2026, 12, 31).toString())
            .header("Link", "</api/v2/books>; rel=\"successor-version\"")
            .body(body)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Одна книга за id")
    fun get(@PathVariable id: Long): BookResponseV1 =
        BookResponseV1.from(bookService.get(id))

    // POST — створити ресурс. Статус 201 + Location header (Слайди 6, 10).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Створити книгу")
    fun create(@Valid @RequestBody request: BookRequestV1): ResponseEntity<BookResponseV1> {
        val created = bookService.create(
            title = request.title,
            author = request.author,
            isbn = request.isbn,
            priceUah = request.priceUah
        )
        return ResponseEntity
            .status(HttpStatus.CREATED)
            // Location вказує URL новоствореного ресурсу
            .header(HttpHeaders.LOCATION, "/api/v1/books/${created.id}")
            .body(BookResponseV1.from(created))
    }

    // PUT — повне оновлення існуючого ресурсу.
    @PutMapping("/{id}")
    @Operation(summary = "Оновити книгу повністю")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: BookRequestV1
    ): BookResponseV1 = BookResponseV1.from(
        bookService.update(id, request.title, request.author, request.isbn, request.priceUah)
    )

    // DELETE — видалення. Статус 204 No Content (Слайд 10).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Видалити книгу")
    fun delete(@PathVariable id: Long) {
        bookService.delete(id)
    }
}
