package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.scene.control.Label;
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

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.LOGIN_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.PRIVATE_USER_CHAT_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.Network.SYSTEM_SOCKET_URL;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;
import static org.mockito.Mockito.*;

public class LoginScreenTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private Stage stage;
    private StageManager stageManager;
    @Mock
    private RestClient restMock;

    @Mock
    private WebSocketClient systemWebSocketClient;

    @Mock
    private WebSocketClient chatWebSocketClient;

    @Mock
    private HttpResponse<JsonNode> resRegister;

    @Mock
    private HttpResponse<JsonNode> resLogin;

    @Mock
    private HttpResponse<JsonNode> res;

    @Mock
    private HttpResponse<JsonNode> resGuestLogin;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptorRegister;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptorLogin;
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
        this.stageManager.initView(STAGE, "Login", "LoginScreen", LOGIN_SCREEN_CONTROLLER, false, null, null);
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
        restMock = null;
        systemWebSocketClient = null;
        chatWebSocketClient = null;
        resRegister = null;
        resLogin = null;
        res = null;
        callbackArgumentCaptor = null;
        callbackArgumentCaptorRegister = null;
        callbackArgumentCaptorLogin = null;
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testSetRememberMe() {
        Options options = new Options();
        stageManager.getResourceManager().loadOptions(options);
        Assert.assertEquals(options.isRememberMe(), false);
        Assert.assertEquals(stageManager.getEditor().getLocalUser().getAccordClient().getOptions().isRememberMe(), false);
        clickOn("#btnRememberMe");
        stageManager.getResourceManager().loadOptions(options);
        Assert.assertEquals(options.isRememberMe(), true);
        Assert.assertEquals(stageManager.getEditor().getLocalUser().getAccordClient().getOptions().isRememberMe(), true);
        clickOn("#btnRememberMe");
        stageManager.getResourceManager().loadOptions(options);
        Assert.assertEquals(options.isRememberMe(), false);
        Assert.assertEquals(stageManager.getEditor().getLocalUser().getAccordClient().getOptions().isRememberMe(), false);
    }

    @Test
    public void startWithRememberMeOption() {
    }

    @Test
    public void startWithoutRememberMeOption() {
    }

    @Test
    public void testBtnOptions() {
        // testing options button
        clickOn("#btnOptions");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Options", this.stageManager.getPopupStage().getTitle());
    }

    @Test
    public void testSuccessfulLogin() {

        String returnMessage = Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder().add("userKey", "c653b568-d987-4331-8d62-26ae617847bf")).build().toString();

        //Mocking of RestClient login function
        when(res.getBody()).thenReturn(new JsonNode(returnMessage));
        //TestFX
        String username = "username";
        String password = "password";

        ((TextField) lookup("#tfUserName").query()).setText(username);

        ((TextField) lookup("#pwUserPw").query()).setText(password);

        clickOn("#btnLogin");

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        Assert.assertEquals("success", res.getBody().getObject().getString("status"));
        Assert.assertEquals("", res.getBody().getObject().getString("message"));
        Assert.assertEquals("c653b568-d987-4331-8d62-26ae617847bf", res.getBody().getObject().getJSONObject("data").getString("userKey"));

        Assert.assertEquals(username, this.stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals("c653b568-d987-4331-8d62-26ae617847bf", this.stageManager.getEditor().getLocalUser().getUserKey());

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Main", stage.getTitle());
    }

    @Test
    public void testGuestLogin() {
        JsonObject returnMessage = Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder().add("name", "Amir Ziaiyan").add("password", "Amir Ziaiyan")).build();

        //Mocking of RestClient guest login function
        when(resGuestLogin.getBody()).thenReturn(new JsonNode(returnMessage.toString()));

        //Shows that the labels for guest user data are empty
        Assert.assertEquals("", ((Label)lookup("#lblGuestPassword").query()).getText());
        Assert.assertEquals("", ((Label)lookup("#lblUserValid").query()).getText());

        clickOn("#btnGuestLogin");

        WaitForAsyncUtils.waitForFxEvents();
        ((TextField) lookup("#tfUserName").query()).setText(returnMessage.getJsonObject(DATA).getString(NAME));
        ((TextField) lookup("#pwUserPw").query()).setText(returnMessage.getJsonObject(DATA).getString(NAME));

        verify(restMock).guestLogin(callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(resGuestLogin);

        //Shows that the data will be updated after creating the guest user via Guest Login button.
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("This user is valid for 24 hours", ((Label)lookup("#lblUserValid").query()).getText());
        Assert.assertEquals("with the password: Amir Ziaiyan", ((Label)lookup("#lblGuestPassword").query()).getText());
        Assert.assertEquals("success", resGuestLogin.getBody().getObject().getString(STATUS));
        Assert.assertEquals("", resGuestLogin.getBody().getObject().getString(MESSAGE));
        Assert.assertEquals("Amir Ziaiyan", resGuestLogin.getBody().getObject().getJSONObject(DATA).getString(NAME));
        Assert.assertEquals("Amir Ziaiyan", resGuestLogin.getBody().getObject().getJSONObject(DATA).getString(PASSWORD));

    }


    @Test
    public void testLoginInvalidCredentials() {
        String returnMessage = Json.createObjectBuilder()
                .add("status", "failure")
                .add("message", "Invalid credentials")
                .add("data", Json.createObjectBuilder())
                .build().toString();

        //Mocking of RestClient login function
        when(res.getBody()).thenReturn(new JsonNode(returnMessage));

        //TestFX
        String username = "username";
        String password = "password";

        ((TextField) lookup("#tfUserName").query()).setText(username);

        ((TextField) lookup("#pwUserPw").query()).setText(password);

        clickOn("#btnLogin");

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("failure", res.getBody().getObject().getString("status"));
        Assert.assertEquals("Invalid credentials", res.getBody().getObject().getString("message"));
        Assert.assertTrue(res.getBody().getObject().getJSONObject("data").isEmpty());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("text-input text-field Error", tfUserName.getStyleClass().toString());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("text-input text-field password-field Error", pwUserPw.getStyleClass().toString());

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Username or password is wrong", errorLabel.getText());

        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getName());
        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getUserKey());
    }

    @Test
    public void testLoginMissingUsername() {
        //TestFX
        String password = "password";

        ((TextField) lookup("#tfUserName").query()).setText("");

        ((TextField) lookup("#pwUserPw").query()).setText(password);

        clickOn("#btnLogin");

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Username or password is missing", errorLabel.getText());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("text-input text-field error", tfUserName.getStyleClass().toString());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("text-input text-field password-field error", pwUserPw.getStyleClass().toString());

        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getName());
        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getUserKey());
    }


    @Test
    public void testLoginMissingPassword() {

        //TestFX
        String username = "username";

        ((TextField) lookup("#tfUserName").query()).setText(username);

        ((TextField) lookup("#pwUserPw").query()).setText("");

        clickOn("#btnLogin");

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Username or password is missing", errorLabel.getText());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("text-input text-field error", tfUserName.getStyleClass().toString());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("text-input text-field password-field error", pwUserPw.getStyleClass().toString());

        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getName());
        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getUserKey());
    }

    @Test
    public void testSuccessfulRegister() {

        //TestFX
        String username = "username";
        String password = "password";

        ((TextField) lookup("#tfUserName").query()).setText(username);

        ((TextField) lookup("#pwUserPw").query()).setText(password);

        clickOn("#btnRegister");

        String returnMessageRegister = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "User created")
                .add("data", Json.createObjectBuilder())
                .build().toString();

        String returnMessageLogin = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("userKey", "c653b568-d987-4331-8d62-26ae617847bf")
                ).build().toString();

        //Mocking of RestClient register function
        when(resRegister.getBody()).thenReturn(new JsonNode(returnMessageRegister));

        when(resLogin.getBody()).thenReturn(new JsonNode(returnMessageLogin));

        verify(restMock).register(anyString(), anyString(), callbackArgumentCaptorRegister.capture());

        Callback<JsonNode> callbackRegister = callbackArgumentCaptorRegister.getValue();
        callbackRegister.completed(resRegister);

        Assert.assertEquals("success", resRegister.getBody().getObject().getString("status"));
        Assert.assertEquals("User created", resRegister.getBody().getObject().getString("message"));
        Assert.assertTrue(resRegister.getBody().getObject().getJSONObject("data").isEmpty());

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptorLogin.capture());

        Callback<JsonNode> callbackLogin = callbackArgumentCaptorLogin.getValue();
        callbackLogin.completed(resLogin);

        Assert.assertEquals("success", resLogin.getBody().getObject().getString("status"));
        Assert.assertEquals("", resLogin.getBody().getObject().getString("message"));
        Assert.assertEquals("c653b568-d987-4331-8d62-26ae617847bf", resLogin.getBody().getObject().getJSONObject("data").getString("userKey"));

        Assert.assertEquals(username, this.stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals("c653b568-d987-4331-8d62-26ae617847bf", this.stageManager.getEditor().getLocalUser().getUserKey());

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Main", stage.getTitle());
    }

    @Test
    public void testRegisterUsernameTaken() {

        String returnMessage = Json.createObjectBuilder()
                .add("status", "failure")
                .add("message", "Name already taken")
                .add("data", Json.createObjectBuilder())
                .build().toString();

        //Mocking of RestClient login function
        when(res.getBody()).thenReturn(new JsonNode(returnMessage));

        //TestFX
        String username = "username";
        String password = "password";

        ((TextField) lookup("#tfUserName").query()).setText(username);

        ((TextField) lookup("#pwUserPw").query()).setText(password);

        clickOn("#btnRegister");

        verify(restMock).register(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("failure", res.getBody().getObject().getString("status"));
        Assert.assertEquals("Name already taken", res.getBody().getObject().getString("message"));
        Assert.assertTrue(res.getBody().getObject().getJSONObject("data").isEmpty());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("text-input text-field Error", tfUserName.getStyleClass().toString());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("text-input text-field password-field Error", pwUserPw.getStyleClass().toString());

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Username already taken", errorLabel.getText());

        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getName());
        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getUserKey());
    }

    @Test
    public void testRegisterMissingUsername() {
        //TestFX
        String password = "password";

        ((TextField) lookup("#tfUserName").query()).setText("");

        ((TextField) lookup("#pwUserPw").query()).setText(password);

        clickOn("#btnRegister");

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Please type in username and password", errorLabel.getText());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("text-input text-field Error", tfUserName.getStyleClass().toString());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("text-input text-field password-field Error", pwUserPw.getStyleClass().toString());

        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getName());
        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getUserKey());
    }

    @Test
    public void testRegisterMissingPassword() {

        //TestFX
        String username = "username";

        ((TextField) lookup("#tfUserName").query()).setText(username);

        ((TextField) lookup("#pwUserPw").query()).setText("");

        clickOn("#btnRegister");

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Please type in username and password", errorLabel.getText());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("text-input text-field Error", tfUserName.getStyleClass().toString());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("text-input text-field password-field Error", pwUserPw.getStyleClass().toString());

        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getName());
        Assert.assertNull(this.stageManager.getEditor().getLocalUser().getUserKey());
    }

}
