package de.uniks.stp.wedoit.accord.client.network;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.TestFactory;

import javax.json.Json;
import javax.json.JsonObject;
import java.net.URI;

public class WebSocketClientTest {
    @TestFactory
    public WebSocketClient generateWebsocketClient(Editor editor, URI uri, WSCallback callback) {
        if (editor == null) {
            editor = new Editor();
            editor.haveAccordClient();
            editor.haveLocalUser("username", "userKey");
        }
        if (uri == null) {
            try {
                uri = new URI("ws://localhost:8080/test");
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            }
        }
        if (callback == null) {
            callback = msg -> {
            };
        }
        Assert.assertNotNull(editor);
        Assert.assertNotNull(uri);
        Assert.assertNotNull(callback);
        return new WebSocketClient(editor, uri, callback);
    }

    @Test
    public void testGetCallback() {
        WSCallback callback = msg -> {
        };
        WebSocketClient webSocketClient = generateWebsocketClient(null, null, callback);
        Assert.assertEquals(callback, webSocketClient.getCallback());
        webSocketClient.stop();
    }

    @Test
    public void testSetCallback() {
        WSCallback newCallback = msg -> {
        };
        WebSocketClient webSocketClient = generateWebsocketClient(null, null, null);
        webSocketClient.setCallback(newCallback);
        Assert.assertEquals(newCallback, webSocketClient.getCallback());
        webSocketClient.stop();
    }

    @Test
    public void testOnMessage() {
        String testPropertyKey = "testProperty";
        String testPropertyValue = "testValue";
        JsonObject messageObject = Json.createObjectBuilder().add(testPropertyKey, testPropertyValue).build();
        String message = JsonUtil.stringify(messageObject);
        WSCallback callback = msg -> Assert.assertEquals(messageObject, msg);
        WebSocketClient webSocketClient = generateWebsocketClient(null, null, callback);
        webSocketClient.onMessage(message);
        webSocketClient.stop();
    }

    @Test
    public void testOnClose() {
        WebSocketClient webSocketClient = generateWebsocketClient(null, null, null);
        webSocketClient.onClose(null, null);
        Assert.assertNull(webSocketClient.getCallback());
        webSocketClient.stop();
    }

    @Test
    public void testSendMessage() {
        WebSocketClient webSocketClient = generateWebsocketClient(null, null, null);
        webSocketClient.sendMessage("TestMessage");
        webSocketClient.stop();
    }

    @Test
    public void testConnectToServer() {
        WebSocketClient webSocketClient = generateWebsocketClient(null, null, null);
        webSocketClient.connectToServer();
    }
}
