package org.example.lab.realtime;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@ServerEndpoint(value = "/ws/updates", configurator = LiveUpdatesEndpoint.Configurator.class)
public class LiveUpdatesEndpoint {
    private static final String HTTP_SESSION = "httpSession";
    private static final String HTTP_SESSION_ID = "httpSessionId";
    private static final String SAME_ORIGIN = "sameOrigin";

    @Inject
    LiveUpdates updates;

    @OnOpen
    public void open(Session socket, EndpointConfig config) throws IOException {
        if (!Boolean.TRUE.equals(config.getUserProperties().get(SAME_ORIGIN))) {
            socket.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Origin not allowed"));
            return;
        }
        Object candidate = config.getUserProperties().get(HTTP_SESSION);
        if (!(candidate instanceof HttpSession httpSession)) {
            reject(socket);
            return;
        }

        try {
            if (httpSession.getAttribute("username") == null) {
                reject(socket);
                return;
            }
            String id = httpSession.getId();
            socket.getUserProperties().put(HTTP_SESSION_ID, id);
            socket.getAsyncRemote().setSendTimeout(5000);
            updates.add(id, socket);
            // The HTTP session may expire between the first check and registration.
            if (httpSession.getAttribute("username") == null) {
                updates.remove(id, socket);
                reject(socket);
            }
        } catch (IllegalStateException expired) {
            updates.remove((String) socket.getUserProperties().get(HTTP_SESSION_ID), socket);
            reject(socket);
        }
    }

    @OnClose
    public void close(Session socket) {
        updates.remove((String) socket.getUserProperties().get(HTTP_SESSION_ID), socket);
    }

    @OnError
    public void error(Session socket, Throwable error) {
        if (socket != null) {
            updates.remove((String) socket.getUserProperties().get(HTTP_SESSION_ID), socket);
        }
    }

    private void reject(Session socket) throws IOException {
        socket.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Authentication required"));
    }

    public static class Configurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request,
                                    HandshakeResponse response) {
            config.getUserProperties().put(HTTP_SESSION, request.getHttpSession());
            config.getUserProperties().put(SAME_ORIGIN, sameOrigin(request.getHeaders()));
        }

        private boolean sameOrigin(Map<String, List<String>> headers) {
            String origin = first(headers, "Origin");
            String host = first(headers, "Host");
            if (origin == null || host == null) return false;
            try {
                URI uri = URI.create(origin);
                return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                        && host.equalsIgnoreCase(uri.getRawAuthority());
            } catch (IllegalArgumentException invalidOrigin) {
                return false;
            }
        }

        private String first(Map<String, List<String>> headers, String name) {
            return headers.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                    .map(Map.Entry::getValue)
                    .filter(values -> !values.isEmpty())
                    .map(values -> values.get(0))
                    .findFirst().orElse(null);
        }
    }
}
