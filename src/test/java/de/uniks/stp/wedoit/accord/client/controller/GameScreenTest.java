package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Chat;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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
import static de.uniks.stp.wedoit.accord.client.constants.Network.PRIVATE_USER_CHAT_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.Network.SYSTEM_SOCKET_URL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GameScreenTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private Stage stage;
    private StageManager stageManager;
    private LocalUser localUser;
    @Mock
    private RestClient restMock;

    @Mock
    private WebSocketClient systemWebSocketClient;

    @Mock
    private WebSocketClient chatWebSocketClient;

    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentSystemCaptorWebSocket;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @Mock
    private HttpResponse<JsonNode> res;

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


        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);
        this.stageManager.showLoginScreen();
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
        systemWebSocketClient = null;
        chatWebSocketClient = null;
        callbackArgumentSystemCaptorWebSocket = null;
        callbackArgumentCaptor = null;
        res = null;
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void gameActionTest(){

        initUserListView();

        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();
        Button btnPlay = lookup("#btnPlay").queryButton();

        Assert.assertEquals("Play", btnPlay.getText());

        lwOnlineUsers.getSelectionModel().select(0);

        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        Assert.assertEquals("Private Chats",stage.getTitle());

        Platform.runLater(() -> this.stageManager.showGameScreen(user));

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(this.stageManager.getGameStage().isShowing());
        Label score = lookup("#lbScore").query();
        Assert.assertEquals("0:0", score.getText());
        Assert.assertEquals(user.getName(), ((Label) lookup("#lbOpponent").query()).getText());

        //play a round until result screen
        //send game action
        clickOn("#btnRock");
        mockChatWebSocket(getTestMessageServerAnswer(user, PREFIX + ROCK));
        WaitForAsyncUtils.waitForFxEvents();


        //receive game action
        mockChatWebSocket(getServerMessageUserAnswer(user, PREFIX + PAPER));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("0:1", ((Label) lookup("#lbScore").query()).getText());

        mockChatWebSocket(getServerMessageUserAnswer(user, PREFIX + ROCK));

        clickOn("#btnPaper");
        mockChatWebSocket(getTestMessageServerAnswer(user, PREFIX + PAPER));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("1:1", ((Label) lookup("#lbScore").query()).getText());

        clickOn("#btnPaper");
        mockChatWebSocket(getTestMessageServerAnswer(user, PREFIX + PAPER));

        mockChatWebSocket(getServerMessageUserAnswer(user, PREFIX + PAPER));
        //1:1 Score


        clickOn("#btnScissors");
        mockChatWebSocket(getTestMessageServerAnswer(user, PREFIX + SCISSORS));

        mockChatWebSocket(getServerMessageUserAnswer(user, PREFIX + PAPER));
        //2:1 Score

        clickOn("#btnScissors");
        mockChatWebSocket(getTestMessageServerAnswer(user, PREFIX + SCISSORS));

        mockChatWebSocket(getServerMessageUserAnswer(user, PREFIX + PAPER));
        WaitForAsyncUtils.waitForFxEvents();

        user.setPrivateChat(new Chat());
        user.setLocalUser(localUser);

        //got to result screen
        Assert.assertEquals("Result",this.stageManager.getGameStage().getTitle());

        clickOn("#btnPlayAgain");
        mockChatWebSocket(getTestMessageServerAnswer(user, GAMEINVITE));

        mockChatWebSocket(getServerMessageUserAnswer(user, GAMEACCEPT));
        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> this.stageManager.showGameScreen(user));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Rock - Paper - Scissors",this.stageManager.getGameStage().getTitle());

        Platform.runLater(() -> this.stageManager.showGameResultScreen(user,false));
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Result",this.stageManager.getGameStage().getTitle());

        //accept a replay
        mockChatWebSocket(getServerMessageUserAnswer(user, GAMEINVITE));

        clickOn("#btnPlayAgain");
        mockChatWebSocket(getTestMessageServerAnswer(user, GAMEACCEPT));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Rock - Paper - Scissors",this.stageManager.getGameStage().getTitle());

        Platform.runLater(() -> this.stageManager.showGameResultScreen(user,true));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#btnQuit");

        Assert.assertEquals("Private Chats",stage.getTitle());

    }

    private void initUserListView() {
        directToPrivateChatsScreen();

        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);

        WaitForAsyncUtils.waitForFxEvents();
    }


    public void directToPrivateChatsScreen() {
        //Mocking of RestClient login function
        JsonObject json = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("userKey", "c653b568-d987-4331-8d62-26ae617847bf"))
                .build();
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

        this.localUser = this.stageManager.getEditor().getLocalUser();

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnPrivateChats");
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

    public JsonObject getServerMessageUserAnswer(User user, String message) {
        return Json.createObjectBuilder()
                .add("channel", "private")
                .add("timestamp", 1614938)
                .add("message", message)
                .add("from", user.getName())
                .add("to", localUser.getName())
                .build();
    }

    public JsonObject getTestMessageServerAnswer(User user, String message) {
        return Json.createObjectBuilder()
                .add("channel", "private")
                .add("timestamp", 1614938)
                .add("message", message)
                .add("from", localUser.getName())
                .add("to", user.getName())
                .build();
    }

}
