package org.example.lab.realtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class LiveUpdates {
    public enum Resource {
        HUMANS("humans"), CARS("cars"), COORDINATES("coordinates");

        private final String path;

        Resource(String path) {
            this.path = path;
        }
    }

    private final ConcurrentMap<String, Set<Session>> connections = new ConcurrentHashMap<>();

    public void add(String httpSessionId, Session socket) {
        connections.compute(httpSessionId, (ignored, sockets) -> {
            if (sockets == null) sockets = ConcurrentHashMap.newKeySet();
            sockets.add(socket);
            return sockets;
        });
    }

    public void remove(String httpSessionId, Session socket) {
        if (httpSessionId == null) return;
        connections.computeIfPresent(httpSessionId, (ignored, sockets) -> {
            sockets.remove(socket);
            return sockets.isEmpty() ? null : sockets;
        });
    }

    public void closeSession(String httpSessionId) {
        Set<Session> sockets = connections.remove(httpSessionId);
        if (sockets == null) return;
        for (Session socket : sockets) {
            try {
                socket.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "HTTP session ended"));
            } catch (IOException | IllegalStateException ignored) {
                // The HTTP session is gone; the connection can no longer receive updates.
            }
        }
    }

    public void publish(Resource... resources) {
        JsonArrayBuilder names = Json.createArrayBuilder();
        for (Resource resource : resources) names.add(resource.path);
        String message = Json.createObjectBuilder()
                .add("type", "data-changed")
                .add("resources", names)
                .build().toString();

        connections.forEach((httpSessionId, sockets) -> {
            for (Session socket : sockets) {
                if (socket.isOpen()) {
                    try {
                        socket.getAsyncRemote().sendText(message, result -> {
                            if (!result.isOK()) disconnect(httpSessionId, socket);
                        });
                    } catch (RuntimeException sendFailure) {
                        disconnect(httpSessionId, socket);
                    }
                } else {
                    remove(httpSessionId, socket);
                }
            }
        });
    }

    private void disconnect(String httpSessionId, Session socket) {
        remove(httpSessionId, socket);
        try {
            socket.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Update delivery failed"));
        } catch (IOException | IllegalStateException ignored) {
            // The socket is already unusable.
        }
    }
}
