package com.xoom.research;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(value = "/events")
public class ServerSocket implements Consumer {
    private final Set<Session> sessions = new HashSet<Session>();

    @OnOpen
    public void onWebSocketConnect(Session session) throws IOException, EncodeException {
        sessions.add(session);  // curious
    }

    @OnMessage
    public void onWebSocketText(String message) throws IOException, EncodeException {
        // this server is never sent messages
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
    }

    @Override
    public void consume(Object o) {
        for (final Session session : sessions) {
            try {
                session.getBasicRemote().sendObject(o.toString());
            } catch (IOException e) {
                sessions.remove(session);
                System.out.printf("Session closed (%s, %s)\n", e.getMessage(), e.getCause());
            } catch (EncodeException e) {
                e.printStackTrace();
            }
        }
    }
}

