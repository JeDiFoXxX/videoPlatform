package ru.videoplatform.signaling.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.videoplatform.signaling.storage.SessionStorage;
import ru.videoplatform.signaling.websocket.WebSocketMessageCodec;

@Component
@RequiredArgsConstructor
public class StudentSignalingHandler implements SignalingHandler {

    private final SessionStorage sessionStorage;
    private final WebSocketMessageCodec messageCodec;

    @Override
    public boolean canHandle(String role) {
        return "ROLE_STUDENT".equals(role);
    }

    @Override
    public void handle(WebSocketSession session, TextMessage message) throws Exception {
        var dto = messageCodec.receiveMessage(message);
        var teacherSession = sessionStorage.getTeacherSession();

        if (teacherSession == null || !teacherSession.isOpen()) {
            var mes = messageCodec.createMessage("reject_call", "teacher_offline");
            session.sendMessage(mes);
            return;
        }

        switch (dto.event()) {
            case "answer_call", "reject_call" -> teacherSession.sendMessage(message);
            default -> { }
        }
    }
}

