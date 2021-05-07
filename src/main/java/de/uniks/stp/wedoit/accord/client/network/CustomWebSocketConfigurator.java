package de.uniks.stp.wedoit.accord.client.network;

import javax.websocket.ClientEndpointConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static de.uniks.stp.wedoit.accord.client.Constants.COM_USER_KEY;

public class CustomWebSocketConfigurator extends ClientEndpointConfig.Configurator {
    private final String userKey;

    public CustomWebSocketConfigurator(String userKey) {
        this.userKey = userKey;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        super.beforeRequest(headers);
        ArrayList<String> key = new ArrayList<>();
        key.add(this.userKey);
        headers.put(COM_USER_KEY, key);
    }
}
