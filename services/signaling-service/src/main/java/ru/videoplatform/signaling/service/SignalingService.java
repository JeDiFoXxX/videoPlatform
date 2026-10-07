package ru.videoplatform.signaling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import ru.videoplatform.signaling.storage.SessionStorage;
import ru.videoplatform.signaling.websocket.WebSocketMessageCodec;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SignalingService {

    private final WebSocketMessageCodec messageCodec;
    private final SessionStorage sessionStorage;

    public void createSession(String userId, String role, WebSocketSession session) throws IOException {
        switch (role) {
            case "ROLE_TEACHER" -> {
                sessionStorage.addTeacherSession(userId, session);
                var studentsIds = sessionStorage.getStudentIdsOnline();
                var message = messageCodec
                        .createMessage("initial_student_list", studentsIds);
                session.sendMessage(message);
            }
            case "ROLE_STUDENT" -> {
                sessionStorage.addStudentSession(userId, session);
                var teacherSession = sessionStorage.getTeacherSession();
                if (teacherSession != null && teacherSession.isOpen()) {
                    var message = messageCodec.createMessage("student_online", userId);
                    teacherSession.sendMessage(message);
                }
            }
            default -> { }
        }
    }

    public void deleteSession(String userId, String role) throws IOException {
        switch (role) {
            case "ROLE_TEACHER" -> sessionStorage.deleteTeacherSession(userId);
            case "ROLE_STUDENT" -> {
                sessionStorage.deleteStudentSession(userId);
                var teacherSession = sessionStorage.getTeacherSession();
                if (teacherSession != null && teacherSession.isOpen()) {
                    var message = messageCodec.createMessage("student_offline", userId);
                    teacherSession.sendMessage(message);
                }
            }
            default -> { }
        }
    }
}
