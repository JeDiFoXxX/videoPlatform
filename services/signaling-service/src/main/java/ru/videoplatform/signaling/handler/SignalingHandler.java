package ru.videoplatform.signaling.handler;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface SignalingHandler {

    boolean canHandle(String role);

    void handle(WebSocketSession session, TextMessage message) throws Exception;

}