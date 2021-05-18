package de.uniks.stp.wedoit.accord.client.controller.serverScreen;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.scene.control.Label;
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
import javax.json.JsonStructure;

import static de.uniks.stp.wedoit.accord.client.Constants.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * tests for the ServerScreenController
 * - user list view
 * - logout
 */
public class ServerScreenControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager stageManager;
    private LocalUser localUser;
    private Server server;
    private JsonStructure msg;

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Mock
    WebSocketClient webSocketClient;

    @Mock
    WebSocketClient chatWebSocketClient;

    @Mock
    WSCallback callback;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentCaptorWebSocket;
    private WSCallback wsCallback;

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
        localUser = stageManager.getEditor().haveLocalUser("John_Doe", "testKey123");
        server = stageManager.getEditor().haveServer(localUser, "testId", "TServer");
        stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClient);
        stageManager.getEditor().getNetworkController().haveWebSocket(CHAT_USER_URL + this.localUser.getName()
                +  AND_SERVER_ID_URL + this.server.getId(),chatWebSocketClient);

        this.stageManager.showServerScreen(server, restMock);

        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    public void mockRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).getExplicitServerInformation(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(webSocketClient).setCallback(callbackArgumentCaptorWebSocket.capture());
        wsCallback = callbackArgumentCaptorWebSocket.getValue();

        wsCallback.handleMessage(webSocketJson);
    }

    @Test
    public void initUserListView() {
        JsonObject restJson = getServerIdSuccessful();
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        ListView listView = lookup("#lvServerUsers").queryListView();
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertEquals(0, listView.getItems().toArray().length);
        mockRest(restJson);
        Assert.assertEquals(3, listView.getItems().toArray().length);
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(0)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(1)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(2)));
        Assert.assertFalse(listView.getItems().contains(new Server()));

        mockWebSocket(webSocketJson);


    }

    @Test
    public void updateUserListView() {
        JsonObject restJson = getServerIdSuccessful();
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        ListView listView = lookup("#lvServerUsers").queryListView();
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertEquals(0, listView.getItems().toArray().length);
        mockRest(restJson);
        Assert.assertEquals(3, listView.getItems().toArray().length);
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(0)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(1)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(2)));
        Assert.assertFalse(listView.getItems().contains(new Server()));


        mockWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(4, listView.getItems().toArray().length);
        Assert.assertEquals(4, server.getMembers().toArray().length);
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(0)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(1)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(2)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(3)));
        Assert.assertFalse(listView.getItems().contains(new Server()));
        User userPhil = null;
        User userI2 = null;
        for (Object user : listView.getItems()) {
            if (user instanceof User) {
                if (((User) user).getName().equals("Phil")) {
                    userPhil = (User) user;
                }
                if (((User) user).getName().equals("N2")) {
                    userI2 = (User) user;
                }
            }
        }
        Assert.assertEquals(true, userPhil.isOnlineStatus());
        Assert.assertEquals(false, userI2.isOnlineStatus());

        wsCallback.handleMessage(webSocketCallbackUserLeft());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(false, userPhil.isOnlineStatus());
        Assert.assertEquals(false, userI2.isOnlineStatus());

    }

    @Test
    public void restClientFailureResponse() {
        JsonObject restJson = getServerIdFailure();
        ListView listView = lookup("#lvServerUsers").queryListView();
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertEquals(0, listView.getItems().toArray().length);
        mockRest(restJson);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(stage.getTitle().equals("Login"));

    }

    @Test
    public void LogoutSuccessfulTest() {
        Assert.assertTrue(stage.getTitle().equals("Server"));
        mockRest(getServerIdSuccessful());
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(logoutSuccessful().toString()));
        clickOn("#btnLogout");
        verify(restMock).logout(anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(stage.getTitle().equals("Login"));


    }

    @Test
    public void logoutFailureTest() {
        Assert.assertTrue(stage.getTitle().equals("Server"));
        mockRest(getServerIdSuccessful());
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(logoutFailure().toString()));
        clickOn("#btnLogout");
        verify(restMock).logout(anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(stage.getTitle().equals("Login"));


    }

    @Test
    public void mainScreenButtonTest() {
        clickOn("#btnHome");
        Assert.assertEquals("Main", stage.getTitle());
    }

    @Test
    public void optionsButtonTest() {
        clickOn("#btnOptions");
        Assert.assertEquals("Options", stageManager.getPopupStage().getTitle());
    }

    @Test
    public void sendChatMessageTest() {
        //init channel list and select first channel
        initUserListView();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lwPrivateChat = lookup("#lvTextChat").queryListView();
        ListView lvServerChannels = lookup("#lvServerChannels").queryListView();

        lvServerChannels.getSelectionModel().select(0);
        Channel channel = (Channel) lvServerChannels.getSelectionModel().getSelectedItem();

        clickOn("#lvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());

        //send message
        clickOn("#tfInputMessage");
        write("Test Message");

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getName(), "Test Message");
        mockWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(channel.getMessages().size(), lwPrivateChat.getItems().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), channel.getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), channel.getMessages().get(0).getText());
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), "Test Message");
    }

    @Test
    public void incomingChatMessageTest() {

    }
    //TODO implement

    @Test
    public void noChannelSelectedTest() {

    }
    //TODO implement

    @Test
    public void testChatMessagesCachedProperlyAfterChatChange() {
    }
    //TODO implement


    // Methods for callbacks

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has joined
     */
    public JsonObject webSocketCallbackUserJoined() {
        return Json.createObjectBuilder().add("action", "userJoined").add("data",
                Json.createObjectBuilder().add("id", "123456").add("name", "Phil")).build();
    }

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has left
     */
    public JsonObject webSocketCallbackUserLeft() {
        return Json.createObjectBuilder().add("action", "userLeft").add("data",
                Json.createObjectBuilder().add("id", "123456").add("name", "Phil")).build();
    }

    public JsonObject getServerIdSuccessful() {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder().add("id", server.getId())
                        .add("name", server.getName()).add("owner", "ow12ner").add("categories",
                                Json.createArrayBuilder()).add("members", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("id", "I1").add("name", "N1")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "I2").add("name", "N2")
                                        .add("online", false))
                                .add(Json.createObjectBuilder().add("id", "I3").add("name", "N3")
                                        .add("online", true)))).build();
    }

    public JsonObject getServerIdFailure() {
        return Json.createObjectBuilder().add("status", "failure").add("message", "")
                .add("data", Json.createObjectBuilder()).build();
    }

    public JsonObject logoutSuccessful() {
        return Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "Logged out")
                .add("data", "{}")
                .build();
    }

    public JsonObject logoutFailure() {
        return Json.createObjectBuilder()
                .add("status", "failure")
                .add("message", "Log in first")
                .add("data", "{}")
                .build();
    }

    public JsonObject getTestMessageServerAnswer(JsonObject test_message) {
        return Json.createObjectBuilder()
                .add("id", "5e2ffbd8770dd077d03dr458")
                .add("channel", "5e2ffbd8770dd077d03dt445")
                .add("timestamp", 1616935874)
                .add("from", localUser.getName())
                .add("message", test_message.getString(COM_MESSAGE))
                .build();
    }

}
