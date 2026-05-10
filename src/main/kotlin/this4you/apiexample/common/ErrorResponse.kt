package this4you.apiexample.common

import java.time.Instant

// === Слайд 10 — Структуроване тіло помилки ===
// Хороше API повертає не просто "Error", а машинно-читабельний JSON:
//   - code: код помилки для frontend (показати правильне повідомлення)
//   - message: пояснення для розробника
//   - traceId: ідентифікатор для пошуку в логах між сервісами
//   - details: список полів з помилками валідації (Слайд 2)
data class ErrorResponse(
    val code: String,
    val message: String,
    val traceId: String?,
    val timestamp: Instant = Instant.now(),
    val details: List<FieldError> = emptyList()
) {
    data class FieldError(val field: String, val message: String)
}
