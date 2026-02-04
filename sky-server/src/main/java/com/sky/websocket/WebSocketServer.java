package com.sky.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketServer extends TextWebSocketHandler {

    // sid -> session
    private static final Map<String, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sid = session.getUri()
                .getPath()
                .substring(session.getUri().getPath().lastIndexOf("/") + 1);

        SESSION_MAP.put(sid, session);
        System.out.println("WebSocket 连接成功：" + sid);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        System.out.println("收到消息：" + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSION_MAP.values().remove(session);
        System.out.println("WebSocket 连接断开");
    }

    // 单发
    public  void sendToOne(String sid, String msg) throws Exception {
        WebSocketSession session = SESSION_MAP.get(sid);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(msg));
        }
    }

    // 群发
    public  void sendToAll(String msg) throws Exception {
        for (WebSocketSession session : SESSION_MAP.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(msg));
            }
        }
    }
}
