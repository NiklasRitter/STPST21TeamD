package de.uniks.stp.wedoit.accord.client.network;

import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import javax.json.Json;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.CATEGORIES;
import static de.uniks.stp.wedoit.accord.client.constants.Network.CHANNELS;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;

public class RestClient {

    /**
     * Login the user with given name and password.
     *
     * @param name     The Name of the user to be logged in.
     * @param password The Password for the USer to be logged in.
     * @param callback The Callback to be called after the Request.
     */
    public void login(String name, String password, Callback<JsonNode> callback) {
        // Build Request Body
        String body = JsonUtil.buildLogin(name, password).toString();

        // Use UniRest to make login request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + LOGIN_PATH)
                .header(NAME, name)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Get all Servers the currently logged in User is Member and/or owner of.
     *
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void getServers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Get the explicit Information of a given Server.
     *
     * @param userKey  The userKey of the currently logged in User.
     * @param serverId The ID of the Server the explicit Information are being requested of.
     * @param callback The Callback to be called after the Request.
     */
    public void getExplicitServerInformation(String userKey, String serverId, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Create a User with a given Name and Password.
     *
     * @param name     The Name of the User to be created.
     * @param password The Password for the User to be created.
     * @param callback The Callback to be called after the Request.
     */
    public void register(String name, String password, Callback<JsonNode> callback) {
        // Build Request Body
        String body = Json.createObjectBuilder().add(NAME, name).add(PASSWORD, password).build().toString();

        // Use UniRest to make register request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH)
                .body(body);
        sendRequest(req, callback);
    }

    /**
     * Log out the currently logged in User.
     *
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void logout(String userKey, Callback<JsonNode> callback) {
        // Use UniRest to make register request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + LOGOUT_PATH)
                .header(USER_KEY, userKey);
        sendRequest(req, callback);
    }

    /**
     * Create a Server with a given Name.
     *
     * @param name     The Name of the Server to be created.
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void createServer(String name, String userKey, Callback<JsonNode> callback) {
        // Build request Body
        String body = Json.createObjectBuilder().add(NAME, name).build().toString();

        // Use UniRest to create server
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH)
                .header(USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Get all Users who are currently online.
     *
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void getOnlineUsers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + USERS_PATH)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Get the Categories of a given Server.
     *
     * @param serverId The ID of the Server the Categories are being requested for.
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void getCategories(String serverId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Create a Category with a given Name.
     *
     * @param name     The Name of the Category to be created.
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void createCategory(String serverId, String name, String userKey, Callback<JsonNode> callback) {
        // Build request Body
        String body = Json.createObjectBuilder().add(NAME, name).build().toString();

        // Use UniRest to create server
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES)
                .header(USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Get the Channels of a given Category in a given Server.
     *
     * @param serverId   The ID of the Server the Category belongs to.
     * @param categoryId The ID of the Category the Channels are being requested for.
     * @param userKey    The userKey of the currently logged in User.
     * @param callback   The Callback to be called after the Request.
     */
    public void getChannels(String serverId, String categoryId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH + categoryId + CHANNELS)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Send a Request and call the Callback asynchronously.
     *
     * @param req      The Request to be sent.
     * @param callback The Callback to be called after the Request.
     */
    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        req.asJsonAsync(callback);
    }
}
