package de.uniks.stp.wedoit.accord.client.network;

import javax.websocket.ClientEndpointConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.USER_KEY;

public class CustomWebSocketConfigurator extends ClientEndpointConfig.Configurator {
    private final String userKey;

    /**
     * Create a new CustomWebsocketConfigurator.
     *
     * @param userKey The userKey of the user currently logged in.
     */
    public CustomWebSocketConfigurator(String userKey) {
        this.userKey = userKey;
    }

    /**
     * Called before the first request.
     * Used to add the userKey to the Websocket Headers.
     *
     * @param headers The Headers of the Websocket.
     */
    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        super.beforeRequest(headers);
        ArrayList<String> key = new ArrayList<>();
        key.add(this.userKey);
        headers.put(USER_KEY, key);
    }
}
