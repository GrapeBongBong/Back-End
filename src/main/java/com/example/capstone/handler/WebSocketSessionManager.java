package com.example.capstone.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private static WebSocketSessionManager instance;
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public static synchronized WebSocketSessionManager getInstance() {
        if (instance == null) {
            instance = new WebSocketSessionManager();
        }
        return instance;
    }

    public void addSession(Long chatRoomId, WebSocketSession session) {
        sessionMap.put(chatRoomId, session);
    }

    public void removeSession(Long chatRoomId) {
        sessionMap.remove(chatRoomId);
    }

    public WebSocketSession getSession(Long chatRoomId) {
        return sessionMap.get(chatRoomId);
    }
}
