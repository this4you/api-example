package this4you.apiexample.graphql

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import this4you.apiexample.books.Book
import this4you.apiexample.books.BookService

// === Слайди 4, 9 — GraphQL controller ===
// Spring for GraphQL мапить методи на типи зі schema.graphqls.
// Той самий BookService, що й у REST/SOAP/gRPC — реальна бізнес-логіка спільна.
@Controller
class BookGraphQLController(private val bookService: BookService) {

    @QueryMapping
    fun books(): List<Book> = bookService.list()

    @QueryMapping
    fun book(@Argument id: Long): Book? = runCatching { bookService.get(id) }.getOrNull()

    @MutationMapping
    fun createBook(@Argument input: CreateBookInput): Book =
        bookService.create(input.title, input.author, input.isbn, input.priceUah)
}

data class CreateBookInput(
    val title: String,
    val author: String,
    val isbn: String,
    val priceUah: Int
)
