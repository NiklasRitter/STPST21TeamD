package de.uniks.stp.wedoit.accord.client.controller.welcomeScreen;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;

import static de.uniks.stp.wedoit.accord.client.Constants.PRIVATE_USER_CHAT_PREFIX;
import static de.uniks.stp.wedoit.accord.client.Constants.SYSTEM_SOCKET_URL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class onlineUserListViewTest extends ApplicationTest {

    private Stage stage;
    private StageManager stageManager;
    private LocalUser localUser;

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Mock
    private WebSocketClient systemWebSocketClient;

    @Mock
    private WebSocketClient chatWebSocketClient;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentSystemCaptorWebSocket;
    private WSCallback wsSystemCallback;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);

        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = stageManager.getEditor().haveLocalUser("Sebastian", "testKey123");

        this.stageManager.getEditor().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + this.localUser.getName(), chatWebSocketClient);

        this.stageManager.showWelcomeScreen(restMock);
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    public void mockRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).getOnlineUsers(anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockSystemWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(systemWebSocketClient).setCallback(callbackArgumentSystemCaptorWebSocket.capture());
        wsSystemCallback = callbackArgumentSystemCaptorWebSocket.getValue();

        wsSystemCallback.handleMessage(webSocketJson);
    }

    @Test
    public void initUserListView() {
        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);

        ListView userListView = lookup("#lwOnlineUsers").queryListView();

        Assert.assertEquals(3, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());

        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(1)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(2)));
    }

    @Test
    public void newUserOnlineListViewUpdated() {
        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        ListView userListView = lookup("#lwOnlineUsers").queryListView();

        Assert.assertEquals(3, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());

        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(1)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(2)));

        mockSystemWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(4, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());

        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(1)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(2)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(3)));
    }

    @Test
    public void userLeftListViewUpdated() {
        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);

        ListView userListView = lookup("#lwOnlineUsers").queryListView();

        JsonObject webSocketJsonUserJoined = webSocketCallbackUserJoined();
        mockSystemWebSocket(webSocketJsonUserJoined);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(4, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));

        JsonObject webSocketJsonUserLeft = webSocketCallbackUserLeft();
        mockSystemWebSocket(webSocketJsonUserLeft);
        WaitForAsyncUtils.waitForFxEvents();

        System.out.println(userListView.getItems());
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(3, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));
    }


    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has joined
     */
    public JsonObject webSocketCallbackUserJoined() {
        return Json.createObjectBuilder()
                .add("action", "userJoined")
                .add("data", Json.createObjectBuilder()
                        .add("id", "123456")
                        .add("name", "Phil"))
                .build();
    }

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has left
     */
    public JsonObject webSocketCallbackUserLeft() {
        return Json.createObjectBuilder()
                .add("action", "userLeft")
                .add("data", Json.createObjectBuilder()
                        .add("id", "123456")
                        .add("name", "Phil"))
                .build();
    }

    public JsonObject getOnlineUsers() {
        return Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "12345")
                                .add("name", "Albert"))
                        .add(Json.createObjectBuilder()
                                .add("id", "5678")
                                .add("name", "Clemens"))
                        .add(Json.createObjectBuilder()
                                .add("id", "203040")
                                .add("name", "Dieter")))
                .build();
    }
}
