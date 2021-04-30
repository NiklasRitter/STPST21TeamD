package de.uniks.stp.wedoit.accord.client.network;

import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import javax.json.Json;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class RestClient {

    public void login(String name, String password, Callback<JsonNode> callback) {
        // Build Request Body
        String body = Json.createObjectBuilder().add(COM_NAME, name).add(COM_PASSWORD, password).build().toString();

        // Use UniRest to make login request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + LOGIN_PATH)
                .header(COM_NAME, name)
                .body(body);

        sendRequest(req, callback);
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        new Thread(() -> req.asJsonAsync(callback)).start();
    }
}
