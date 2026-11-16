package org.example.lab.realtime;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/updates")
public class LiveUpdatesEndpoint {
    private static final Set<Session> SESSIONS = new CopyOnWriteArraySet<>();

    @OnOpen
    public void open(Session socket) {
        SESSIONS.add(socket);
    }

    @OnClose
    public void close(Session socket) {
        SESSIONS.remove(socket);
    }

    @OnError
    public void error(Session socket, Throwable error) {
        if (socket != null) {
            SESSIONS.remove(socket);
        }
    }

    static void broadcast(String message) {
        for (Session socket : SESSIONS) {
            if (socket.isOpen()) {
                socket.getAsyncRemote().sendText(message);
            }
        }
    }
}
