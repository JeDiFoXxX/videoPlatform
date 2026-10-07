package ru.videoplatform.signaling.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.videoplatform.signaling.storage.SessionStorage;
import ru.videoplatform.signaling.websocket.WebSocketMessageCodec;

@Component
@RequiredArgsConstructor
public class TeacherSignalingHandler implements SignalingHandler {

    private final SessionStorage sessionStorage;
    private final WebSocketMessageCodec messageCodec;

    @Override
    public boolean canHandle(String role) {
        return "ROLE_TEACHER".equals(role);
    }

    @Override
    public void handle(WebSocketSession session, TextMessage message) throws Exception {
        var dto = messageCodec.receiveMessage(message);
        var studentSession = sessionStorage.getStudentSession(dto.studentId());
        boolean isStudentOnline = studentSession != null && studentSession.isOpen();

        switch (dto.event()) {
            case "start_call" -> {
                if (isStudentOnline) {
                    studentSession.sendMessage(message);
                } else {
                    var mes = messageCodec.createMessage("reject_call", "student_offline");
                    session.sendMessage(mes);
                }
            }
            case "reject_call" -> {
                if (isStudentOnline) {
                    studentSession.sendMessage(message);
                }
            }
            default -> { }
        }
    }
}
