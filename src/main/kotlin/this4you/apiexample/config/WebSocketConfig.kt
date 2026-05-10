package this4you.apiexample.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import this4you.apiexample.chat.ChatWebSocketHandler

// === Слайд 5 — WebSocket endpoint ===
// Реєструємо handler на шляху /ws/chat. Клієнт підключається через
// JavaScript: new WebSocket("ws://localhost:8080/ws/chat") (див. chat.html)
@Configuration
@EnableWebSocket
class WebSocketConfig(private val chatHandler: ChatWebSocketHandler) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatHandler, "/ws/chat")
            .setAllowedOrigins("*")  // у production обмежуй конкретні домени
    }
}
