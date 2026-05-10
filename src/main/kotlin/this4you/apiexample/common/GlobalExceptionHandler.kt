package this4you.apiexample.common

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

// === Слайд 10 — Обробка помилок і статус-коди ===
// Один централізований обробник перетворює виключення в правильний HTTP статус
// з структурованим тілом ErrorResponse. Це робить помилки передбачуваними для
// frontend та monitoring (вони орієнтуються саме на HTTP статус, а не на текст).
@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    // 404 Not Found — ресурс не знайдено
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> =
        respond(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.message ?: "Resource not found")

    // 400 Bad Request — невалідні дані запиту (валідація DTO зі Слайду 2)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map {
            ErrorResponse.FieldError(it.field, it.defaultMessage ?: "invalid")
        }
        val body = ErrorResponse(
            code = "VALIDATION_ERROR",
            message = "Request validation failed",
            traceId = MDC.get("traceId"),
            details = fieldErrors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    // 409 Conflict — наприклад, спроба створити Book з існуючим ISBN
    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ResponseEntity<ErrorResponse> =
        respond(HttpStatus.CONFLICT, "CONFLICT", ex.message ?: "Conflict")

    // 500 Internal Server Error — все, що не передбачено
    // У реальному API не варто показувати клієнту stack trace — лише traceId.
    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception", ex)
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Something went wrong")
    }

    private fun respond(status: HttpStatus, code: String, message: String): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(code = code, message = message, traceId = MDC.get("traceId"))
        return ResponseEntity.status(status).body(body)
    }
}

class ResourceNotFoundException(message: String) : RuntimeException(message)
class ConflictException(message: String) : RuntimeException(message)
