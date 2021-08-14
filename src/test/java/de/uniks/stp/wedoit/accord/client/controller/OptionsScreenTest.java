
package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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
import java.util.Locale;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
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

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.oldOptions = new Options();
        stageManager.getResourceManager().loadOptions(oldOptions);
        stageManager.getResourceManager().saveOptions(new Options().setDarkmode(false).setRememberMe(false));
        stageManager.getResourceManager().saveOptions(new Options().setLanguage("en_GB").setOutputDevice(null).setInputDevice(null));
        this.stageManager.start(stage);
        this.popupStage = this.stageManager.getStage(StageEnum.POPUP_STAGE);

        //create localUser to skip the login screen
        stageManager.getEditor().haveLocalUser("John_Doe", "testKey123");
        stageManager.getEditor().getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "5e2ffbd8770dd077d03df506", webSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);

        this.stageManager.initView(ControllerEnum.LOGIN_SCREEN, true, null);

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

        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#btnLogin");

        WaitForAsyncUtils.waitForFxEvents();

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> callbackLogin = callbackArgumentCaptor.getValue();
        callbackLogin.completed(res);
    }

    @Test
    public void testBtnDarkmode() {
        // open options screen
        press(KeyCode.CONTROL, KeyCode.O);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Options - Appearance", stage.getTitle());
        press(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertFalse("Options - Appearance".equals(stage.getTitle()));
        directToOptionsScreen();
        // check if stylesheets contain light theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(stage.isShowing());
        Assert.assertEquals("Options - Appearance", stage.getTitle());
        Assert.assertTrue(stageManager.getScene(StageEnum.STAGE).getStylesheets()
                .contains(Objects.requireNonNull(StageManager.class.getResource("light-theme.css")).toExternalForm()));

        // test darkmode button
        clickOn("#btnDarkMode");

        // check if stylesheets contain dark theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(stageManager.getScene(StageEnum.STAGE).getStylesheets()
                .contains(Objects.requireNonNull(StageManager.class.getResource("dark-theme.css")).toExternalForm()));

        Assert.assertTrue(stageManager.getPrefManager().loadDarkMode());

        press(KeyCode.CONTROL, KeyCode.D);

        // check if stylesheets contain dark theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertFalse(stageManager.getScene(StageEnum.STAGE).getStylesheets()
                .contains(Objects.requireNonNull(StageManager.class.getResource("dark-theme.css")).toExternalForm()));

    }

    @Test
    public void testChoiceBoxLanguage() {
        Assert.assertEquals(Locale.getDefault().getLanguage(), "en_gb");
        // open options screen
        directToOptionsScreen();

        Assert.assertTrue(stage.isShowing());
        Assert.assertEquals("Options - Appearance", stage.getTitle());
        clickOn("Language");

        // check if stylesheets contain light theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(stage.isShowing());
        Assert.assertEquals("Options - Language", stage.getTitle());
        Assert.assertEquals(Locale.getDefault().getLanguage(), "en_gb");

        //Label lblLanguage = lookup("#lblLanguage").query();
        //Assert.assertEquals(lblLanguage.getText(), "Language");

        //ChoiceBox<String> choiceBoxLanguage = lookup("#choiceBoxLanguage").query();

        clickOn("Deutsch");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(Locale.getDefault().getLanguage(), "de_de");
        Assert.assertEquals("Einstellungen - Sprache", stage.getTitle());

        clickOn("Zurück");

        directToMainScreen();
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(Locale.getDefault().getLanguage(), "de_de");

        Assert.assertEquals("Private Chats", stage.getTitle());

        // open options screen
        directToOptionsScreen();
        clickOn("Sprache");
        WaitForAsyncUtils.waitForFxEvents();
        //ChoiceBox<String> choiceBoxLanguage2 = lookup("#choiceBoxLanguage").query();
        clickOn("English");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Options - Language", stage.getTitle());
        Assert.assertEquals(Locale.getDefault().getLanguage(), "en_gb");

    }

    @Test
    public void testLogoutButtonOnLoginScreenNotVisible() {
        directToOptionsScreen();
        try {
            lookup("#btnLogout").query();
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals("Options - Appearance", stage.getTitle());
        }
    }

    @Test
    public void testLogoutButtonOnMainScreenVisible() {
        directToMainScreen();

        // got to main screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Private Chats", stage.getTitle());
        directToOptionsScreen();

        Assert.assertEquals("Options - Appearance", stage.getTitle());

        Assert.assertNotNull(lookup("#btnAppearance").query());
        Assert.assertNotNull(lookup("#btnLanguage").query());
        Assert.assertNotNull(lookup("#btnConnections").query());
        Assert.assertNotNull(lookup("#btnVoice").query());
        Assert.assertNotNull(lookup("#btnDescription").query());
        Assert.assertNotNull(lookup("#btnLogout").query());
    }

    @Test
    public void inputOutputDeviceSelect() {
        Options options = this.stageManager.getEditor().getAccordClient().getOptions();
        options.setInputDevice(null);
        options.setOutputDevice(null);
        directToMainScreen();
        WaitForAsyncUtils.waitForFxEvents();

        directToOptionsScreen();
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Sound");

        Assert.assertNull(stageManager.getEditor().getAccordClient().getOptions().getOutputDevice());
        Assert.assertNull(stageManager.getEditor().getAccordClient().getOptions().getInputDevice());
        ChoiceBox<String> outputDeviceChoiceBox = lookup("#choiceBoxOutputDevice").query();
        outputDeviceChoiceBox.getItems().add("test");
        Platform.runLater(() -> outputDeviceChoiceBox.getSelectionModel().select("test"));
        clickOn(outputDeviceChoiceBox);
        Platform.runLater(() -> outputDeviceChoiceBox.getSelectionModel().select(1));
        ChoiceBox<String> inputDeviceChoiceBox = lookup("#choiceBoxInputDevice").query();
        inputDeviceChoiceBox.getItems().add("test");
        Platform.runLater(() -> inputDeviceChoiceBox.getSelectionModel().select("test"));
        clickOn(inputDeviceChoiceBox);
        Platform.runLater(() -> inputDeviceChoiceBox.getSelectionModel().select(1));

        clickOn("Back");
        directToOptionsScreen();
        clickOn("Sound");
    }

    @Test
    public void zoomLevelTest() {
        Options options = this.stageManager.getEditor().getAccordClient().getOptions();
        directToMainScreen();
        WaitForAsyncUtils.waitForFxEvents();
        options.setZoomLevel(50);
        int zoomLevel = options.getZoomLevel();
        press(KeyCode.CONTROL, KeyCode.PLUS).release(KeyCode.CONTROL, KeyCode.PLUS);
        Assert.assertEquals(zoomLevel + 25, options.getZoomLevel());
        press(KeyCode.CONTROL, KeyCode.MINUS).release(KeyCode.CONTROL, KeyCode.MINUS);
        Assert.assertEquals(zoomLevel - 25, options.getZoomLevel());

    }


}
