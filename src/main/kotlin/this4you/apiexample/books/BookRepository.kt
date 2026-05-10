package this4you.apiexample.books

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

// === In-memory репозиторій ===
// Це демо-проект, тому ми не використовуємо реальну БД.
// ConcurrentHashMap дозволяє безпечно працювати з кількох потоків.
@Repository
class BookRepository {
    private val store = ConcurrentHashMap<Long, Book>()
    private val nextId = AtomicLong(1)

    @PostConstruct
    fun seed() {
        // Стартові дані для демо
        save(Book(0, "Kotlin in Action",     "Dmitry Jemerov", "9781617293290", 850))
        save(Book(0, "Effective Java",       "Joshua Bloch",   "9780134685991", 1200))
        save(Book(0, "Clean Architecture",   "Robert Martin",  "9780134494166", 950))
    }

    fun findAll(): List<Book> = store.values.sortedBy { it.id }

    fun findById(id: Long): Book? = store[id]

    fun existsByIsbn(isbn: String): Boolean =
        store.values.any { it.isbn == isbn }

    fun save(book: Book): Book {
        val id = if (book.id == 0L) nextId.getAndIncrement() else book.id
        val saved = book.copy(id = id)
        store[id] = saved
        return saved
    }

    fun deleteById(id: Long): Boolean = store.remove(id) != null
}
