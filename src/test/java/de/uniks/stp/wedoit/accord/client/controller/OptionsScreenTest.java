package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.PreferenceManager;
import de.uniks.stp.wedoit.accord.client.util.ResourceManager;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
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
import java.util.Locale;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.LOGIN_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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
    private PreferenceManager preferenceManager;
    private ResourceManager resourceManager;

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
        this.oldOptions = new Options();
        stageManager.getResourceManager().loadOptions(oldOptions);
        stageManager.getResourceManager().saveOptions(new Options().setDarkmode(false).setRememberMe(false));
        stageManager.getResourceManager().saveOptions(new Options().setLanguage("en_GB"));
        this.stageManager.start(stage);
        this.popupStage = this.stageManager.getPopupStage();

        //create localUser to skip the login screen
        stageManager.getEditor().haveLocalUser("John_Doe", "testKey123");
        stageManager.getEditor().getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "5e2ffbd8770dd077d03df506", webSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);
        this.stageManager.initView(STAGE, "Login", "LoginScreen", LOGIN_SCREEN_CONTROLLER, true, null, null);
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() throws Exception {
        stageManager.getResourceManager().saveOptions(this.oldOptions);
        oldOptions = null;
        super.stop();
        stage = null;
        popupStage = null;
        stageManager.stop();
        stageManager = null;
        resourceManager = null;
        preferenceManager = null;
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
        clickOn("#btnDarkMode");

        // check if stylesheets contain dark theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(stageManager.getScene().getStylesheets()
                .contains(Objects.requireNonNull(StageManager.class.getResource("dark-theme.css")).toExternalForm()));
    }

    @Test
    public void testChoiceBoxLanguage() {
        Label lblEnterUserName = lookup("#lblEnterUserName").query();
        Assert.assertEquals(lblEnterUserName.getText(), "Enter your username");
        Assert.assertEquals(Locale.getDefault().getLanguage(), "en_gb");
        // open options screen
        directToOptionsScreen();

        // check if stylesheets contain light theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(popupStage.isShowing());
        Assert.assertEquals("Options", popupStage.getTitle());
        Assert.assertEquals(Locale.getDefault().getLanguage(), "en_gb");

        Label lblLanguage = lookup("#lblLanguage").query();
        Assert.assertEquals(lblLanguage.getText(), "Language");

        ChoiceBox choiceBoxLanguage = lookup("#choiceBoxLanguage").query();

        clickOn(choiceBoxLanguage);

        Platform.runLater(() -> {
            //choice german as language
            choiceBoxLanguage.getSelectionModel().select(1);
        });

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(Locale.getDefault().getLanguage(), "de_de");

        Assert.assertEquals(lblLanguage.getText(), "Sprache");

        Platform.runLater(() -> {
            popupStage.hide();
        });

        directToMainScreen();

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(Locale.getDefault().getLanguage(), "de_de");
        Label lblYourServers = lookup("#lblYourServers").query();
        Assert.assertEquals(lblYourServers.getText(), "Ihre Server");

        // open options screen
        directToOptionsScreen();

        Platform.runLater(() -> {
            //choice english as language
            choiceBoxLanguage.getSelectionModel().select(0);
        });

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(lblLanguage.getText(), "Language");

        Platform.runLater(() -> {
            popupStage.hide();
        });

        Assert.assertEquals(Locale.getDefault().getLanguage(), "en_gb");
    }

    @Test
    public void testLogoutButtonOnLoginScreenNotVisible() {
        directToOptionsScreen();
        VBox mainVBox = (VBox) lookup("#mainVBox").query();

        Assert.assertEquals(mainVBox.getChildren().size(), 4);
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

        Assert.assertEquals(mainVBox.getChildren().size(), 4);
        Assert.assertTrue(btnLogout.isVisible());
    }


}
