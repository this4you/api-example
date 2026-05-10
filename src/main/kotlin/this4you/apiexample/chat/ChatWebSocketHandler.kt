package this4you.apiexample.chat

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.CopyOnWriteArraySet

// === Слайд 5 — WebSocket ===
// На відміну від HTTP (запит → відповідь), WebSocket створює постійне
// двостороннє з'єднання. Сервер може сам "штовхати" дані клієнту.
// Так працюють чати, live-нотифікації, біржі.
//
// Цей handler — простий чат: коли хтось пише — повідомлення розсилається
// усім підключеним сесіям (broadcast).
@Component
class ChatWebSocketHandler : TextWebSocketHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    // CopyOnWriteArraySet безпечний для одночасних читань і модифікацій
    private val sessions = CopyOnWriteArraySet<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        log.info("WS connected: {} (total: {})", session.id, sessions.size)
        broadcast("[system] new client joined")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        log.info("WS message from {}: {}", session.id, message.payload)
        broadcast("[${session.id.take(6)}] ${message.payload}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
        log.info("WS disconnected: {}", session.id)
    }

    private fun broadcast(text: String) {
        val payload = TextMessage(text)
        sessions.forEach { s ->
            if (s.isOpen) runCatching { s.sendMessage(payload) }
        }
    }
}
