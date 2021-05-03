package de.uniks.stp.wedoit.accord.client.network;

import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import javax.json.Json;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class RestClient {

    private static void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        new Thread(() -> req.asJsonAsync(callback)).start();
    }

    public static void login(String name, String password, Callback<JsonNode> callback) {
        // Build Request Body
        String body = Json.createObjectBuilder().add(COM_NAME, name).add(COM_PASSWORD, password).build().toString();

        // Use UniRest to make login request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + LOGIN_PATH)
                .header(COM_NAME, name)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Request to get all servers where the user with the given userKey is member or owner
     *
     * @param userKey userKey of the logged in user
     * @param callback callback
     */
    public void getServers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH)
                .header(COM_USERKEY, userKey);

        sendRequest(req, callback);
    }
}
