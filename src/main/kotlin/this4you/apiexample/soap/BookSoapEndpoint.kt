package this4you.apiexample.soap

import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload
import this4you.apiexample.books.BookService

// === Слайд 4 — SOAP endpoint ===
// Spring WS мапить XML-повідомлення на методи через @PayloadRoot:
// якщо локальне ім'я кореневого елемента == "GetBookRequest" і namespace співпадає,
// викликається цей метод. Маршалінг XML ↔ Kotlin виконує Jaxb2Marshaller.
@Endpoint
class BookSoapEndpoint(private val bookService: BookService) {

    @PayloadRoot(namespace = "http://this4you/apiexample/soap/books", localPart = "GetBookRequest")
    @ResponsePayload
    fun getBook(@RequestPayload request: GetBookRequest): GetBookResponse {
        val book = bookService.get(request.id)
        return GetBookResponse().apply {
            id = book.id
            title = book.title
            author = book.author
            isbn = book.isbn
            priceUah = book.priceUah
        }
    }
}
