package ru.videoplatform.signaling.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.videoplatform.signaling.handler.SignalingHandler;
import ru.videoplatform.signaling.service.SignalingService;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SignalingWebSocketHandler extends TextWebSocketHandler {

    private final SignalingService signalingService;
    private final List<SignalingHandler> signalingHandlers;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var userId = Objects.requireNonNull(session.getAttributes().get("userId")).toString();
        var role = Objects.requireNonNull(session.getAttributes().get("userRole")).toString();
        try {
            signalingService.createSession(userId, role, session);
        } catch (Exception e) {
            try {
                signalingService.deleteSession(userId, role);
            } catch (Exception ignored) { }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        var userId = Objects.requireNonNull(session.getAttributes().get("userId")).toString();
        var role = Objects.requireNonNull(session.getAttributes().get("userRole")).toString();
        try {
            signalingService.deleteSession(userId, role);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        var role = Objects.requireNonNull(session.getAttributes().get("userRole")).toString();
        var handler = signalingHandlers.stream()
                .filter(h -> h.canHandle(role))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
        try {
            handler.handle(session, message);
        } catch (Exception ignored) { }
    }
}
