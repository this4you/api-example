package this4you.apiexample.grpc

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import this4you.apiexample.books.BookService
import this4you.apiexample.common.ResourceNotFoundException
import this4you.apiexample.grpc.proto.BookByIdRequest
import this4you.apiexample.grpc.proto.BookGrpcApiGrpc
import this4you.apiexample.grpc.proto.BookListRequest
import this4you.apiexample.grpc.proto.BookListResponse
import this4you.apiexample.grpc.proto.BookMessage

// === Слайд 4 — gRPC сервіс ===
// gRPC — це бінарний RPC від Google. Відрізняється від REST:
//   - формат: Protocol Buffers (бінарний, компактний),
//   - транспорт: HTTP/2,
//   - підтримує streaming в обидва боки.
// Зазвичай використовується для service-to-service комунікації у мікросервісах.
//
// Перевірити: grpcurl -plaintext localhost:9090 book.BookGrpcApi/List
//
// У тестах вимикаємо через demo.grpc.enabled=false (інакше порт 9090 буде
// зайнятий другим тестовим контекстом → BindException).
@Component
@ConditionalOnProperty(value = ["demo.grpc.enabled"], havingValue = "true", matchIfMissing = true)
class BookGrpcService(private val bookService: BookService) :
    BookGrpcApiGrpc.BookGrpcApiImplBase() {

    private val log = LoggerFactory.getLogger(javaClass)
    private var server: Server? = null

    @PostConstruct
    fun start() {
        // gRPC слухає на окремому порту, не на 8080 (де REST/GraphQL/SOAP)
        server = ServerBuilder.forPort(9090)
            .addService(this)
            .build()
            .start()
        log.info("gRPC server started on port 9090")
    }

    @PreDestroy
    fun stop() {
        server?.shutdown()
    }

    override fun list(request: BookListRequest, responseObserver: StreamObserver<BookListResponse>) {
        val response = BookListResponse.newBuilder()
            .addAllBooks(bookService.list().map { it.toMessage() })
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun get(request: BookByIdRequest, responseObserver: StreamObserver<BookMessage>) {
        try {
            val book = bookService.get(request.id)
            responseObserver.onNext(book.toMessage())
            responseObserver.onCompleted()
        } catch (ex: ResourceNotFoundException) {
            // У gRPC є власні статус-коди — аналог HTTP 4xx/5xx (Слайд 10)
            responseObserver.onError(
                io.grpc.Status.NOT_FOUND.withDescription(ex.message).asRuntimeException()
            )
        }
    }

    private fun this4you.apiexample.books.Book.toMessage(): BookMessage =
        BookMessage.newBuilder()
            .setId(id)
            .setTitle(title)
            .setAuthor(author)
            .setIsbn(isbn)
            .setPriceUah(priceUah)
            .build()
}
