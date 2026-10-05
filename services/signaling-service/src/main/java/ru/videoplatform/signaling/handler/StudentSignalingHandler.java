package ru.videoplatform.signaling.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.videoplatform.signaling.service.SignalingService;
import ru.videoplatform.signaling.websocket.WebSocketMessageCodec;

@Component
@RequiredArgsConstructor
public class StudentSignalingHandler implements SignalingHandler {

    private final SignalingService signalingService;
    private final WebSocketMessageCodec messageCodec;

    @Override
    public boolean canHandle(String role) {
        return "ROLE_STUDENT".equals(role);
    }

    @Override
    public void handle(WebSocketSession session, TextMessage message) throws Exception {
        var dto = messageCodec.receiveMessage(message);
        var teacherSession = signalingService.getSession(dto.teacherId(), "ROLE_TEACHER");
        boolean isTeacherOnline = teacherSession != null && teacherSession.isOpen();

        if (!isTeacherOnline) {
            var mes = messageCodec.createMessage("reject_call", "teacher_offline");
            session.sendMessage(mes);
            return;
        }

        switch (dto.event()) {
            case "answer_call" -> teacherSession.sendMessage(message);
            case "reject_call" -> {
                teacherSession.sendMessage(message);
            }
            default -> { }
        }
    }
}

