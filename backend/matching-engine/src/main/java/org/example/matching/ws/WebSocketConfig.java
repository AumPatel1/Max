package org.example.matching.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // turns on ws in spring
public class WebSocketConfig implements WebSocketConfigurer {

    private final MarketWebSocketHandler handler;

    public WebSocketConfig(MarketWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Connect via ws://host:8080/ws/market
        // if /ws/market is reached , use this handler to have the buisness logic
        registry.addHandler(handler, "/ws/market")
                .setAllowedOrigins("*");
    }
}
