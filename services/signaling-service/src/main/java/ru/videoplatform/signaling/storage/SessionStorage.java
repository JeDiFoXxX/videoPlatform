package ru.videoplatform.signaling.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStorage {

    private final Map<String, WebSocketSession> teacherSessions = new ConcurrentHashMap<>(1);
    private final Map<String, WebSocketSession> studentSessions = new ConcurrentHashMap<>();

    public void addTeacherSession(String userId, WebSocketSession session) {
        teacherSessions.put(userId, session);
    }

    public void addStudentSession(String userId, WebSocketSession session) {
        studentSessions.put(userId, session);
    }

    public WebSocketSession getTeacherSession() {
        return teacherSessions.values().stream()
                .findFirst()
                .orElse(null);
    }

    public WebSocketSession getStudentSession(String userId) {
        return studentSessions.get(userId);
    }

    @SuppressWarnings("resource")
    public void deleteTeacherSession(String userId) {
        teacherSessions.remove(userId);
    }

    @SuppressWarnings("resource")
    public void deleteStudentSession(String userId) {
        studentSessions.remove(userId);
    }

    public Set<String> getStudentIdsOnline() {
        return studentSessions.keySet();
    }
}
