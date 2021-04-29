package de.uniks.stp.wedoit.accord.client.network;

import javax.json.Json;
import kong.unirest.*;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class RestClient {

    public static void login(String name, String password, Callback<JsonNode> callback) {
        // Build Request Body
        String body = Json.createObjectBuilder().add(COM_NAME, name).add(COM_PASSWORD, password).build().toString();

        // Use UniRest to make login request
        HttpRequest<?> req = Unirest.post( REST_SERVER_URL + USERS_PATH + LOGIN_PATH)
                .header(COM_NAME, name)
                .body(body);

        sendRequest(req, callback);
    }

    private static void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        new Thread(() -> req.asJsonAsync(callback)).start();
    }
}
