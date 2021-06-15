package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
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

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.MESSAGE;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.TO;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.PRIVATE_USER_CHAT_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.Network.SYSTEM_SOCKET_URL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PrivateChatsScreenTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private Stage stage;
    private Stage emojiPickerStage;
    private StageManager stageManager;
    private LocalUser localUser;
    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;
    @Mock
    private WebSocketClient systemWebSocketClient;
    @Mock
    private WebSocketClient chatWebSocketClient;

    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentSystemCaptorWebSocket;

    private Editor editor;

    @BeforeClass
    public static void before() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);

        this.emojiPickerStage = this.stageManager.getEmojiPickerStage();

        StageManager.getEditor().getNetworkController().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        StageManager.getEditor().getNetworkController().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        StageManager.getEditor().getNetworkController().setRestClient(restMock);
        StageManager.showLoginScreen();
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() {
        rule = null;
        stage = null;
        stageManager = null;
        localUser = null;
        restMock = null;
        res = null;
        emojiPickerStage = null;
        callbackArgumentCaptor = null;
        systemWebSocketClient = null;
        chatWebSocketClient = null;
        callbackArgumentSystemCaptorWebSocket = null;
        editor = null;
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void initUserListView() {
        directToPrivateChatsScreen();

        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);

        ListView<User> userListView = lookup("#lwOnlineUsers").queryListView();

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(3, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());

        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(1)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(2)));
        Assert.assertFalse(userListView.getItems().get(0).isChatRead());
        Assert.assertFalse(userListView.getItems().get(1).isChatRead());
        Assert.assertFalse(userListView.getItems().get(2).isChatRead());
    }

    @Test
    public void newUserOnlineListViewUpdated() {
        directToPrivateChatsScreen();

        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        ListView<User> userListView = lookup("#lwOnlineUsers").queryListView();
        WaitForAsyncUtils.waitForFxEvents();
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
        Assert.assertFalse(userListView.getItems().get(0).isChatRead());
        Assert.assertFalse(userListView.getItems().get(1).isChatRead());
        Assert.assertFalse(userListView.getItems().get(2).isChatRead());
        Assert.assertFalse(userListView.getItems().get(3).isChatRead());
    }

    @Test
    public void userLeftListViewUpdated() {
        directToPrivateChatsScreen();

        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);

        ListView<User> userListView = lookup("#lwOnlineUsers").queryListView();

        JsonObject webSocketJsonUserJoined = webSocketCallbackUserJoined();
        mockSystemWebSocket(webSocketJsonUserJoined);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(4, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));

        JsonObject webSocketJsonUserLeft = webSocketCallbackUserLeft();
        mockSystemWebSocket(webSocketJsonUserLeft);
        WaitForAsyncUtils.waitForFxEvents();


        Assert.assertEquals(3, userListView.getItems().size());
        Assert.assertEquals(StageManager.getEditor().getOnlineUsers().size(), userListView.getItems().size());
    }

    @Test
    public void testGameInvite() {
        initUserListView();

        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();
        Button btnPlay = lookup("#btnPlay").queryButton();

        Assert.assertEquals("Play", btnPlay.getText());

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        clickOn(btnPlay);
        WaitForAsyncUtils.waitForFxEvents();

        //send game invite
        JsonObject gameInvite = JsonUtil.buildPrivateChatMessage(user.getName(), GAMEINVITE);
        mockChatWebSocket(getTestMessageServerAnswer(gameInvite));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(1, localUser.getGameRequests().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(INVITE, lwPrivateChat.getItems().get(0).getText());

        //receive game accepted message
        mockChatWebSocket(getServerMessageUserAnswer(user, GAMEACCEPT));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(StageManager.getGameStage().isShowing());
        Assert.assertEquals("Rock - Paper - Scissors", StageManager.getGameStage().getTitle());


        mockChatWebSocket(getServerMessageUserAnswer(user, PREFIX + ROCK));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#btnPaper");
        JsonObject gameAction = JsonUtil.buildPrivateChatMessage(user.getName(), PREFIX + PAPER);
        mockChatWebSocket(getTestMessageServerAnswer(gameAction));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testGameAccept() {
        //init user list and select first user
        initUserListView();
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();
        Button btnPlay = lookup("#btnPlay").queryButton();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        //receive invite message
        mockChatWebSocket(getServerMessageUserAnswer(user, GAMEINVITE));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Accept", btnPlay.getText());
        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(1, localUser.getGameInvites().size());
        Assert.assertEquals(0, localUser.getGameRequests().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(INVITE, lwPrivateChat.getItems().get(0).getText());

        clickOn(btnPlay);

        JsonObject gameAccept = JsonUtil.buildPrivateChatMessage(user.getName(), GAMEACCEPT);
        mockChatWebSocket(getTestMessageServerAnswer(gameAccept));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(StageManager.getGameStage().isShowing());
        Assert.assertEquals("Rock - Paper - Scissors", StageManager.getGameStage().getTitle());

    }

    @Test
    public void testChatSendMessage() {
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

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnEmoji");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(emojiPickerStage.isShowing());
        Assert.assertEquals("Emoji Picker", emojiPickerStage.getTitle());

        GridPane panelForEmojis = (GridPane) emojiPickerStage.getScene().getRoot().lookup("#panelForEmojis");
        EmojiButton emoji = (EmojiButton) panelForEmojis.getChildren().get(0);
        clickOn(emoji);

        //send message
        clickOn("#tfEnterPrivateChat");
        write("Test Message");
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), "Test Message" + emoji.getText());
        mockChatWebSocket(getTestMessageServerAnswer(test_message));

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(user.getPrivateChat().getMessages().size(), lwPrivateChat.getItems().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals("Test Message" + emoji.getText(), lwPrivateChat.getItems().get(0).getText());
    }

    @Test
    public void testChatIncomingMessage() {
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
        mockChatWebSocket(getServerMessageUserAnswer(user, "Hallo"));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(user.getPrivateChat().getMessages().size(), lwPrivateChat.getItems().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals("Hallo", lwPrivateChat.getItems().get(0).getText());
    }

    @Test
    public void testChatIncomingMessageNoUserSelected() {
        initUserListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        User user = lwOnlineUsers.getItems().get(0);


        //receive message
        mockChatWebSocket(getServerMessageUserAnswer(user, "Hallo"));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertFalse(lwOnlineUsers.getItems().get(0).isChatRead());
    }

    @Test
    public void testChatNoUserSelected() {
        //init user list and select first user
        initUserListView();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();

        //send message
        ((TextField) lookup("#tfEnterPrivateChat").query()).setText("Test Message\n");

        Assert.assertEquals(0, lwPrivateChat.getItems().size());
    }

    @Test
    public void testChatMessagesCachedProperlyAfterChatChange() {
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
        ((TextField) lookup("#tfEnterPrivateChat").query()).setText("Test Message");

        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lwPrivateChat.getItems().size());
        Assert.assertEquals(user.getPrivateChat().getMessages().size(), lwPrivateChat.getItems().size());
        Assert.assertEquals(lwPrivateChat.getItems().get(0), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(0).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals("Test Message", lwPrivateChat.getItems().get(0).getText());

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
        Assert.assertEquals("Test Message", lwPrivateChat.getItems().get(0).getText());
    }

    public void mockRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock, atLeastOnce()).getOnlineUsers(anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockChatWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(chatWebSocketClient).setCallback(callbackArgumentSystemCaptorWebSocket.capture());
        WSCallback wsSystemCallback = callbackArgumentSystemCaptorWebSocket.getValue();

        wsSystemCallback.handleMessage(webSocketJson);
    }


    @Test
    public void testQuote() {
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

        WaitForAsyncUtils.waitForFxEvents();

        //send message
        clickOn("#tfEnterPrivateChat");
        write("Test Message");
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        lwPrivateChat.getSelectionModel().select(0);
        rightClickOn(lwPrivateChat);
        Bounds boundsInLocal = (lookup("#messageContextMenu").query()).localToScreen((lookup("#messageContextMenu").query().getBoundsInLocal()));
        clickOn(boundsInLocal.getCenterX(), boundsInLocal.getCenterY());
        WaitForAsyncUtils.waitForFxEvents();
        Label lblQuote = (Label) lookup("#lblQuote").query();
        Button btnCancelQuote = (Button) lookup("#btnCancelQuote").query();

        String formatted = StageManager.getEditor().getMessageFormatted(lwPrivateChat.getItems().get(0));
        Assert.assertEquals(lblQuote.getText(), formatted);
        clickOn(btnCancelQuote);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(lblQuote.getText(), "");


        lwPrivateChat.getSelectionModel().select(0);
        rightClickOn(lwPrivateChat);
        boundsInLocal = (lookup("#messageContextMenu").query()).localToScreen((lookup("#messageContextMenu").query().getBoundsInLocal()));
        clickOn(boundsInLocal.getCenterX(), boundsInLocal.getCenterY());
        WaitForAsyncUtils.waitForFxEvents();
        lblQuote = (Label) lookup("#lblQuote").query();
        btnCancelQuote = (Button) lookup("#btnCancelQuote").query();

        formatted = StageManager.getEditor().getMessageFormatted(lwPrivateChat.getItems().get(0));
        Assert.assertEquals(lblQuote.getText(), formatted);

        ((TextField) lookup("#tfEnterPrivateChat").query()).setText("quote");
        clickOn("#tfEnterPrivateChat");
        write("\n");

        WaitForAsyncUtils.waitForFxEvents();
        JsonObject quote = JsonUtil.buildPrivateChatMessage(user.getName(), QUOTE_PREFIX + formatted + QUOTE_ID + "123" + QUOTE_SUFFIX);
        JsonObject quote_message = JsonUtil.buildPrivateChatMessage(user.getName(), "quote");
        mockChatWebSocket(getTestMessageServerAnswer(quote));
        mockChatWebSocket(getTestMessageServerAnswer(quote_message));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(lwPrivateChat.getItems().get(1).getText(), QUOTE_PREFIX + formatted + QUOTE_ID + "123" + QUOTE_SUFFIX);
        Assert.assertEquals(lwPrivateChat.getItems().get(2).getText(), "quote");

        lwPrivateChat.getSelectionModel().select(1);
        clickOn(lwPrivateChat);
        Assert.assertEquals(lwPrivateChat.getSelectionModel().getSelectedItem(),lwPrivateChat.getItems().get(0));
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
                .add("message", test_message.getString(MESSAGE))
                .add("from", localUser.getName())
                .add("to", test_message.getString(TO))
                .build();
    }

    public JsonObject getServerMessageUserAnswer(User user, String message) {
        return Json.createObjectBuilder()
                .add("channel", "private")
                .add("timestamp", 1614938)
                .add("message", message)
                .add("from", user.getName())
                .add("to", localUser.getName())
                .build();
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


    public void mockSystemWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(systemWebSocketClient).setCallback(callbackArgumentSystemCaptorWebSocket.capture());
        WSCallback wsSystemCallback = callbackArgumentSystemCaptorWebSocket.getValue();

        wsSystemCallback.handleMessage(webSocketJson);
    }

    public void directToPrivateChatsScreen() {
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

        ((TextField) lookup("#tfUserName").query()).setText(username);

        ((TextField) lookup("#pwUserPw").query()).setText(password);

        clickOn("#btnLogin");

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callbackLogin = callbackArgumentCaptor.getValue();
        callbackLogin.completed(res);

        this.localUser = StageManager.getEditor().getLocalUser();

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnPrivateChats");
    }

    @Test
    public void testBtnLogout() {

        JsonObject json = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "Logged out")
                .add("data", "{}")
                .build();
        when(res.getBody()).thenReturn(new JsonNode(json.toString()));

        directToPrivateChatsScreen();

        // got to privateChats screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Private Chats", stage.getTitle());

        // testing logout button
        // first have to open optionScreen
        clickOn("#btnOptions");
        Assert.assertEquals("Options", stageManager.getPopupStage().getTitle());

        clickOn("#btnLogout");

        verify(restMock).logout(anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callbackLogout = callbackArgumentCaptor.getValue();
        callbackLogout.completed(res);

        Assert.assertEquals("success", res.getBody().getObject().getString("status"));

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Login", stage.getTitle());
    }

    @Test
    public void testBtnOptions() {

        directToPrivateChatsScreen();

        // got to privateChats screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Private Chats", stage.getTitle());

        // testing options button
        clickOn("#btnOptions");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Options", StageManager.getPopupStage().getTitle());
    }

    @Test
    public void testBtnHome() {

        directToPrivateChatsScreen();

        // got to privateChats screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Private Chats", stage.getTitle());

        // testing home button
        clickOn("#btnHome");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Main", stage.getTitle());
    }

    @Test
    public void testOnlineUserListViewInit() {

        directToPrivateChatsScreen();

        String returnMessage = Json.createObjectBuilder()
                .add("status", "success").add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df505")
                                .add("name", "Albert")
                        )
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df506")
                                .add("name", "Clemens"))
                ).build().toString();

        when(res.getBody()).thenReturn(new JsonNode(returnMessage));
    }
}
