package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Chat;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.PRIVATE_USER_CHAT_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.Network.SYSTEM_SOCKET_URL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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
    private Options oldOptions;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.oldOptions = new Options();
        stageManager.getResourceManager().loadOptions(oldOptions);
        stageManager.getResourceManager().saveOptions(new Options().setRememberMe(false));
        stageManager.getResourceManager().saveOptions(new Options().setLanguage("en_GB"));
        this.stageManager.start(stage);


        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);
        this.stageManager.initView(ControllerEnum.LOGIN_SCREEN, true, null);
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() {
        stageManager.getResourceManager().saveOptions(this.oldOptions);
        oldOptions = null;
        rule = null;
        stage = null;
        stageManager.stop();
        stageManager = null;
        localUser = null;
        restMock = null;
        systemWebSocketClient = null;
        chatWebSocketClient = null;
        callbackArgumentSystemCaptorWebSocket = null;
        callbackArgumentCaptor = null;
        res = null;
    }

    @Test
    public void gameActionTest() {

        initUserListView();

        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();
        Button btnPlay = lookup("#btnPlay").queryButton();

        Assert.assertEquals(LanguageResolver.getString("PLAY"), btnPlay.getText());

        lwOnlineUsers.getSelectionModel().select(0);

        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        Assert.assertEquals("Private Chats", stage.getTitle());

        Platform.runLater(() -> this.stageManager.initView(ControllerEnum.GAME_SCREEN_INGAME, user, null));

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(this.stageManager.getStage(StageEnum.GAME_STAGE).isShowing());
        Label score = lookup("#lbScore").query();
        Assert.assertEquals("0:0", score.getText());
        Assert.assertEquals(user.getName(), ((Label) lookup("#lbOpponent").query()).getText());

        //play a round until result screen
        //send game action
        clickOn("#btnRock");
        mockChatWebSocket(getTestMessageServerAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_ROCK));
        WaitForAsyncUtils.waitForFxEvents();


        //receive game action
        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_PAPER));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("0:1", ((Label) lookup("#lbScore").query()).getText());

        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_ROCK));

        clickOn("#btnPaper");
        mockChatWebSocket(getTestMessageServerAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_PAPER));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("1:1", ((Label) lookup("#lbScore").query()).getText());

        clickOn("#btnPaper");
        mockChatWebSocket(getTestMessageServerAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_PAPER));

        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_PAPER));
        //1:1 Score


        clickOn("#btnScissors");
        mockChatWebSocket(getTestMessageServerAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_SCISSORS));

        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_PAPER));
        //2:1 Score

        clickOn("#btnScissors");
        mockChatWebSocket(getTestMessageServerAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_SCISSORS));

        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_PREFIX + GAME_CHOOSE_MOVE + GAME_PAPER));
        WaitForAsyncUtils.waitForFxEvents();

        user.setPrivateChat(new Chat());
        user.setLocalUser(localUser);

        //got to result screen
        Assert.assertEquals("Result", this.stageManager.getStage(StageEnum.GAME_STAGE).getTitle());

        clickOn("#btnPlayAgain");
        mockChatWebSocket(getTestMessageServerAnswer(user, GAME_REVENGE));

        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_REVENGE));
        WaitForAsyncUtils.waitForFxEvents();

        //Platform.runLater(() -> this.stageManager.initView(ControllerEnum.GAME_SCREEN_INGAME, user, null));
        //WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Rock - Paper - Scissors", this.stageManager.getStage(StageEnum.GAME_STAGE).getTitle());

        Platform.runLater(() -> this.stageManager.initView(ControllerEnum.GAME_SCREEN_RESULT, user, false));
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Result", this.stageManager.getStage(StageEnum.GAME_STAGE).getTitle());

        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_REVENGE));

        //accept a replay
        clickOn("#btnPlayAgain");
        //receive game accepted message
        mockChatWebSocket(getTestMessageServerAnswer(user, GAME_REVENGE));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Rock - Paper - Scissors", this.stageManager.getStage(StageEnum.GAME_STAGE).getTitle());

        Platform.runLater(() -> this.stageManager.initView(ControllerEnum.GAME_SCREEN_RESULT, user, true));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#btnQuit");

        Assert.assertEquals("Private Chats", stage.getTitle());

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
    }


    public JsonObject getOnlineUsers() {
        return Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "12345")
                                .add("name", "Albert")
                                .add("description", ""))
                        .add(Json.createObjectBuilder()
                                .add("id", "5678")
                                .add("name", "Clemens")
                                .add("description", ""))
                        .add(Json.createObjectBuilder()
                                .add("id", "203040")
                                .add("name", "Dieter")
                                .add("description", "")))
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
