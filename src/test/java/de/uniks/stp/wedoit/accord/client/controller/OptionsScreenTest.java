package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.ResourceManager;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.PRIVATE_USER_CHAT_PREFIX;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OptionsScreenTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private RestClient restMock;
    @Mock
    private HttpResponse<JsonNode> res;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;
    @Mock
    private WebSocketClient webSocketClient;
    @Mock
    private WebSocketClient systemWebSocketClient;
    @Mock
    private WebSocketClient chatWebSocketClient;

    private Stage stage;
    private Stage popupStage;
    private StageManager stageManager;
    private Options oldOptions;

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
        this.oldOptions = stageManager.getResourceManager().loadOptions();
        stageManager.getResourceManager().saveOptions(new Options().setDarkmode(false));

        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);
        this.popupStage = StageManager.getPopupStage();

        //create localUser to skip the login screen
        stageManager.getEditor().haveLocalUser("John_Doe", "testKey123");
        stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "5e2ffbd8770dd077d03df506", webSocketClient);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        this.stageManager.getEditor().getNetworkController().setRestClient(restMock);
        StageManager.showLoginScreen();
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() throws Exception {
        stageManager.getResourceManager().saveOptions(this.oldOptions);
        super.stop();
        stage = null;
        popupStage = null;
        stageManager = null;
        oldOptions = null;
        rule = null;
        restMock = null;
        res = null;
        callbackArgumentCaptor = null;
        webSocketClient = null;
        systemWebSocketClient = null;
        chatWebSocketClient = null;
    }

    public void directToOptionsScreen() {
        clickOn("#btnOptions");
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

        ((TextField) lookup("#tfUserName").query()).setText(username);

        ((TextField) lookup("#pwUserPw").query()).setText(password);

        clickOn("#btnLogin");

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callbackLogin = callbackArgumentCaptor.getValue();
        callbackLogin.completed(res);
    }

    @Test
    public void testBtnDarkmode() {
        // open options screen
        directToOptionsScreen();

        // check if stylesheets contain light theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(popupStage.isShowing());
        Assert.assertEquals("Options", popupStage.getTitle());
        Assert.assertTrue(stageManager.getScene().getStylesheets()
                .contains(Objects.requireNonNull(StageManager.class.getResource("light-theme.css")).toExternalForm()));

        // test darkmode button
        clickOn("#btnDarkmode");

        // check if stylesheets contain dark theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(stageManager.getScene().getStylesheets()
                .contains(Objects.requireNonNull(StageManager.class.getResource("dark-theme.css")).toExternalForm()));

    }

    @Test
    public void testLogoutButtonOnLoginScreenNotVisible() {
        directToOptionsScreen();
        VBox mainVBox = (VBox) lookup("#mainVBox").query();

        // Assert that in Login screen the Logout button is not visible and it has correct size
        Assert.assertEquals(mainVBox.getHeight(), 80, 0);
        Assert.assertEquals(mainVBox.getWidth(), 300, 0);
        Assert.assertEquals(mainVBox.getChildren().size(), 1);
    }

    @Test
    public void testLogoutButtonOnMainScreenVisible() {
        directToMainScreen();

        // got to main screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Main", stage.getTitle());
        directToOptionsScreen();

        VBox mainVBox = (VBox) lookup("#mainVBox").query();
        Button btnLogout = (Button) lookup("#btnLogout").query();

        Assert.assertEquals(mainVBox.getChildren().size(), 2);
        Assert.assertTrue(btnLogout.isVisible());
        Assert.assertEquals(mainVBox.getHeight(), 150, 0);
        Assert.assertEquals(mainVBox.getWidth(), 300, 0);
    }
}
