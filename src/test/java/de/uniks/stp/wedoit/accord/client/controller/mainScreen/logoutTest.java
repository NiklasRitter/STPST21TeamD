package de.uniks.stp.wedoit.accord.client.controller.mainScreen;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
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

public class logoutTest extends ApplicationTest {
    private Stage stage;
    private StageManager stageManager;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);

        this.stageManager.getEditor().getNetworkController().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        StageManager.showLoginScreen(restMock);
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Mock
    private WebSocketClient systemWebSocketClient;

    @Mock
    private WebSocketClient chatWebSocketClient;

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    public void directToMainScreen() {
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
    }

    @Test
    public void testBtnLogout() {
        JsonObject json = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "Logged out")
                .add("data", "{}")
                .build();
        when(res.getBody()).thenReturn(new JsonNode(json.toString()));

        directToMainScreen();

        // got to main screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Main", stage.getTitle());

        // testing logout button
        clickOn("#btnLogout");

        verify(restMock).logout(anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callbackLogout = callbackArgumentCaptor.getValue();
        callbackLogout.completed(res);

        Assert.assertEquals("success", res.getBody().getObject().getString("status"));

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Login", stage.getTitle());
    }


}
