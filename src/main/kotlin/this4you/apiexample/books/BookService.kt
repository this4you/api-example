package this4you.apiexample.books

import org.springframework.stereotype.Service
import this4you.apiexample.common.ConflictException
import this4you.apiexample.common.ResourceNotFoundException

// === Service шар ===
// Бізнес-логіка живе тут, а не в контролерах. Це робить її переносимою:
// той самий сервіс ми використаємо у REST (v1, v2), GraphQL, gRPC, SOAP контролерах.
@Service
class BookService(private val repository: BookRepository) {

    fun list(): List<Book> = repository.findAll()

    fun get(id: Long): Book =
        repository.findById(id)
            ?: throw ResourceNotFoundException("Book with id=$id not found")

    fun create(title: String, author: String, isbn: String, priceUah: Int): Book {
        // Слайд 10 — 409 Conflict, якщо ISBN зайнятий
        if (repository.existsByIsbn(isbn)) {
            throw ConflictException("Book with ISBN=$isbn already exists")
        }
        return repository.save(Book(0, title, author, isbn, priceUah))
    }

    fun update(id: Long, title: String, author: String, isbn: String, priceUah: Int): Book {
        get(id)  // кинути 404, якщо не існує
        return repository.save(Book(id, title, author, isbn, priceUah))
    }

    fun delete(id: Long) {
        if (!repository.deleteById(id)) {
            throw ResourceNotFoundException("Book with id=$id not found")
        }
    }
}
