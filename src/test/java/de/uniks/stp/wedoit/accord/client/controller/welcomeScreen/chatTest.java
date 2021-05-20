package de.uniks.stp.wedoit.accord.client.controller.welcomeScreen;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
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

public class chatTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
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
    private WebSocketClient systemWebSocketClient;
    @Mock
    private WebSocketClient chatWebSocketClient;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentSystemCaptorWebSocket;
    private WSCallback wsSystemCallback;

    private Editor editor;

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
        this.editor = stageManager.getEditor();

        this.stageManager.getEditor().getNetworkController().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        this.stageManager.getEditor().getNetworkController().setRestClient(restMock);
        StageManager.showLoginScreen();
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }


    @Test
    public void testChatSendMessage() {

        directToWelcomeScreen();

        //init user list and select first user
        initUserListView();
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        //send message
        clickOn("#tfEnterPrivateChat");
        write("Test Message");

        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(user.getPrivateChat().getMessages().size(), lwPrivateChat.getItems().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), "Test Message");
    }

    @Test
    public void testChatIncomingMessage() {
        directToWelcomeScreen();

        initUserListView();
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();


        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        //receive message
        mockChatWebSocket(getServerMessageUserAnswer(user));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(user.getPrivateChat().getMessages().size(), lwPrivateChat.getItems().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), "Hallo");
    }

    @Test
    public void testChatNoUserSelected() {
        directToWelcomeScreen();

        //init user list and select first user
        initUserListView();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();

        //send message
        clickOn("#tfEnterPrivateChat");
        write("Test Message\n");

        Assert.assertEquals(0, lwPrivateChat.getItems().size());
    }

    @Test
    public void testChatMessagesCachedProperlyAfterChatChange() {
        directToWelcomeScreen();

        //init user list and select first user
        initUserListView();
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        //send message
        clickOn("#tfEnterPrivateChat");
        write("Test Message");

        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(user.getPrivateChat().getMessages().size(), lwPrivateChat.getItems().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), "Test Message");

        lwOnlineUsers.getSelectionModel().select(1);
        User user1 = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user1.getName(), lblSelectedUser.getText());

        Assert.assertEquals(0, lwPrivateChat.getItems().size());

        clickOn("#lwPrivateChat");

        lwOnlineUsers.getSelectionModel().select(0);
        User user2 = lwOnlineUsers.getSelectionModel().getSelectedItem();
        clickOn("#lwOnlineUsers");

        Assert.assertEquals(user2.getName(), lblSelectedUser.getText());

        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(user2.getPrivateChat().getMessages().size(), lwPrivateChat.getItems().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), user2.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), user2.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), "Test Message");
    }

    public void mockRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).getOnlineUsers(anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }


    public void mockChatWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(chatWebSocketClient).setCallback(callbackArgumentSystemCaptorWebSocket.capture());
        wsSystemCallback = callbackArgumentSystemCaptorWebSocket.getValue();

        wsSystemCallback.handleMessage(webSocketJson);
    }


    public void initUserListView() {
        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);

        WaitForAsyncUtils.waitForFxEvents();

        ListView<User> userListView = lookup("#lwOnlineUsers").queryListView();

        Assert.assertEquals(3, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());

        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(1)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(2)));
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

    public JsonObject getTestMessageServerAnswer(JsonObject test_message) {
        return Json.createObjectBuilder()
                .add("channel", "private")
                .add("timestamp", 1614938)
                .add("message", test_message.getString(COM_MESSAGE))
                .add("from", localUser.getName())
                .add("to", test_message.getString(COM_TO))
                .build();
    }

    public JsonObject getServerMessageUserAnswer(User user) {
        return Json.createObjectBuilder()
                .add("channel", "private")
                .add("timestamp", 1614938)
                .add("message", "Hallo")
                .add("from", user.getName())
                .add("to", localUser.getName())
                .build();
    }

    public void directToWelcomeScreen() {

        //Mocking of RestClient login function
        JsonObject json = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("userKey", "c653b568-d987-4331-8d62-26ae617847bf")
                ).build();

        when(res.getBody()).thenReturn(new JsonNode(json.toString()));

        //TestFX
        String username = "username";
        String password = "password";

        clickOn("#tfUserName");
        write(username);

        clickOn("#pwUserPw");
        write(password);

        clickOn("#btnLogin");

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callbackLogin = callbackArgumentCaptor.getValue();
        callbackLogin.completed(res);

        this.localUser = editor.getLocalUser();

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnWelcome");
    }
}
