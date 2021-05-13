package de.uniks.stp.wedoit.accord.client.network;

import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import javax.json.Json;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class RestClient {

    public void login(String name, String password, Callback<JsonNode> callback) {
        // Build Request Body
        String body = JsonUtil.buildLogin(name, password).toString();

        // Use UniRest to make login request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + LOGIN_PATH)
                .header(COM_NAME, name)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Request to get all servers where the user with the given userKey is member or owner
     *
     * @param userKey  userKey of the logged in user
     * @param callback callback
     */
    public void getServers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH)
                .header(COM_USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Request to get explicit information to the server with given id
     *
     * @param userKey  userKey of the logged in user
     * @param callback callback
     */
    public void getExplicitServerInformation(String userKey,String serverId, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId)
                .header(COM_USER_KEY, userKey);

        sendRequest(req, callback);
    }

    public void register(String name, String password, Callback<JsonNode> callback) {
        // Build Request Body
        String body = Json.createObjectBuilder().add(COM_NAME, name).add(COM_PASSWORD, password).build().toString();

        // Use UniRest to make register request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH)
                .body(body);
        sendRequest(req, callback);
    }

    public void logout(String userKey, Callback<JsonNode> callback) {
        // Use UniRest to make register request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + LOGOUT_PATH)
                .header(COM_USER_KEY, userKey);
        sendRequest(req, callback);
    }

    public void createServer(String name, String userKey, Callback<JsonNode> callback){
        // Build request Body
        String body = Json.createObjectBuilder().add(COM_NAME, name).build().toString();

        // Use UniRest to create server
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH)
                .header(COM_USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    public void getOnlineUsers(String userKey,Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + USERS_PATH)
                .header(COM_USER_KEY, userKey);

        sendRequest(req, callback);
    }

    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        req.asJsonAsync(callback);
    }
}
