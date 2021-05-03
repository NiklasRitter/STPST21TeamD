package de.uniks.stp.wedoit.accord.client.network;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;

import javax.json.JsonObject;
import javax.websocket.*;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketClient extends Endpoint {
    private final Editor editor;
    private Session session;
    private Timer noopTimer;
    private WSCallback callback;

    public WebSocketClient(Editor editor, URI endpoint, WSCallback callback) {
        this.editor = editor;
        this.noopTimer = new Timer();

        try {
            ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().build();

            WebSocketContainer WebSocketContainer = ContainerProvider.getWebSocketContainer();
            WebSocketContainer.connectToServer(this, clientEndpointConfig, endpoint);
            this.callback = callback;
        } catch (Exception e) {
            System.err.println("Error while establishing websocket connection:");
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        this.session.addMessageHandler(String.class, this::onMessage);

        this.noopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Send NOOP Message
                sendMessage("noop");
            }
        }, 0, 1000 * 30);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        this.noopTimer.cancel();
        this.session = null;
    }

    public void onMessage(String message) {
        JsonObject jsonMessage = JsonUtil.parse(message);

        this.callback.handleMessage(jsonMessage);
    }

    public void sendMessage(String message) {
        if (this.session.isOpen()) {
            this.session.getAsyncRemote().sendText(message);
        }
    }

    public void stop() {
        this.noopTimer.cancel();
        try {
            this.session.close();
        } catch (Exception ignored) {
        }
    }
}
