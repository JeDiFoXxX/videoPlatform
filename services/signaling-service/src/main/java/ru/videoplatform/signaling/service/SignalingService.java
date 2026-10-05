package ru.videoplatform.signaling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import ru.videoplatform.signaling.websocket.WebSocketMessageCodec;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SignalingService {

    private final WebSocketMessageCodec messageCodec;

    private final Map<String, WebSocketSession> teacherSessions = new ConcurrentHashMap<>(1);
    private final Map<String, WebSocketSession> studentSessions = new ConcurrentHashMap<>();

    public void createSession(String userId, String role, WebSocketSession session) throws IOException {
        switch (role) {
            case "ROLE_TEACHER" -> {
                teacherSessions.put(userId, session);
                var message = messageCodec.createMessage("initial_student_list", studentSessions.keySet());
                session.sendMessage(message);
            }
            case "ROLE_STUDENT" -> {
                studentSessions.put(userId, session);
                if (!teacherSessions.isEmpty()) {
                    var teacherSession = teacherSessions.values().iterator().next();
                    var message = messageCodec.createMessage("student_online", userId);
                    try {
                        teacherSession.sendMessage(message);
                    } catch (Exception ignored) { }
                }
            }
            default -> { }
        }
    }

    public WebSocketSession getSession(String userId, String role) {
        return switch (role) {
            case "ROLE_TEACHER" -> teacherSessions.get(userId);
            case "ROLE_STUDENT" -> studentSessions.get(userId);
            default -> null;
        };
    }

    @SuppressWarnings("resource")
    public void deleteSession(String userId, String role) throws IOException {
        switch (role) {
            case "ROLE_TEACHER" -> teacherSessions.remove(userId);
            case "ROLE_STUDENT" -> {
                studentSessions.remove(userId);
                if (!teacherSessions.isEmpty()) {
                    var teacherSession = teacherSessions.values().iterator().next();
                    var message = messageCodec.createMessage("student_offline", userId);
                    teacherSession.sendMessage(message);
                }
            }
            default -> { }
        }
    }
}
