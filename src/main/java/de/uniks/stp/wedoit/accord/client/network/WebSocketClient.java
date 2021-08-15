package de.uniks.stp.wedoit.accord.client.network;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;

import javax.json.JsonObject;
import javax.websocket.*;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

@ClientEndpoint
public class WebSocketClient extends Endpoint {
    private final Editor editor;
    private final Timer noopTimer;
    private Session session;
    private WSCallback callback;
    private ClientEndpointConfig clientEndpointConfig;
    private final URI endpoint;

    /**
     * Create a new WebsocketClient.
     *
     * @param editor   The Editor of the Application.
     * @param endpoint The URI the WebsocketClient listens to.
     * @param callback The Callback to be called when a message is received.
     */
    public WebSocketClient(Editor editor, URI endpoint, WSCallback callback) {
        this.editor = editor;
        this.noopTimer = new Timer();
        this.callback = callback;
        this.endpoint = endpoint;

        try {
            this.clientEndpointConfig =
                    ClientEndpointConfig.Builder.create().configurator(new CustomWebSocketConfigurator(this.editor.getLocalUser().getUserKey())).build();
        } catch (Exception e) {
            System.err.println("Error while preparing websocket connection:");
            e.printStackTrace();
        }
    }

    /**
     * Connects to server
     */
    public WebSocketClient connectToServer() {
        try {
            WebSocketContainer WebSocketContainer = ContainerProvider.getWebSocketContainer();
            WebSocketContainer.connectToServer(this, clientEndpointConfig, endpoint);
        } catch (Exception e) {
            System.err.println("Error while establishing websocket connection:");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Get the Callback of this WebsocketClient.
     *
     * @return THe current Callback of this websocketClient.
     */
    public WSCallback getCallback() {
        return callback;
    }

    /**
     * Set the Callback of this WebsocketClient.
     *
     * @param callback The new callback this WebsocketClient is supposed to call.
     */
    public void setCallback(WSCallback callback) {
        this.callback = callback;
    }

    /**
     * Called when this WebsocketClient is opened.
     *
     * @param session The Session of this WebsocketClient.
     * @param config  The Configuration of this WebsocketClient.
     */
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        if (this.session != null) this.session.addMessageHandler(String.class, this::onMessage);

        this.noopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Send NOOP Message
                sendMessage("noop");
            }
        }, 0, 1000 * 30);
    }

    /**
     * Called when this WebsocketClient is closed.
     *
     * @param session     The Session of this WebsocketClient.
     * @param closeReason The Reason the Connection was closed for.
     */
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        this.noopTimer.cancel();
        this.session = null;
        this.callback = null;
    }

    /**
     * Called when this WebsocketClient receives a message.
     * <p>
     * Calls the Callback with the parsed Message.
     *
     * @param message The message received from the Server.
     */
    public void onMessage(String message) {
        JsonObject jsonMessage = JsonUtil.parse(message);

        this.callback.handleMessage(jsonMessage);
    }

    /**
     * Send a message to the Server.
     *
     * @param message The Message to be sent.
     */
    public void sendMessage(String message) {
        if (this.session != null && this.session.isOpen()) {
            this.session.getAsyncRemote().sendText(message);
        }
    }

    /**
     * Stops this WebsocketClient.
     * <p>
     * Cancels the timer and closes the Session.
     */
    public void stop() {
        this.noopTimer.cancel();
        try {
            this.session.close();
        } catch (Exception ignored) {
        }
    }
}
