package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RegisterTest extends ApplicationTest {

    private Stage stage;
    private StageManager stageManager;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);
        StageManager.showLoginScreen(restMock);
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> resRegister;

    @Mock
    private HttpResponse<JsonNode> resLogin;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptorRegister;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptorLogin;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSuccessfulRegister() {

        //TestFX
        String username = "username";
        String password = "password";

        clickOn("#tfUserName");
        write(username);

        clickOn("#pwUserPw");
        write(password);

        clickOn("#btnRegister");

        //Mocking of RestClient register function
        when(resRegister.getBody()).thenReturn(new JsonNode("{" +
                "\"status\": \"success\",\n" +
                " \"message\": \"User created\",\n" +
                " \"data\": {}\n}"));

        when(resLogin.getBody()).thenReturn(new JsonNode("{" +
                "\"status\": \"success\",\n" +
                " \"message\": \"\",\n" +
                " \"data\": {\n" +
                " \"userKey\": \"c653b568-d987-4331-8d62-26ae617847bf\"\n" +
                " }" +
                "}"));

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

        Assert.assertEquals(username, stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals("c653b568-d987-4331-8d62-26ae617847bf", stageManager.getEditor().getLocalUser().getUserKey());

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(stage.getTitle(), "Main");
    }

    @Test
    public void testUsernameTaken() {

        //Mocking of RestClient login function
        when(res.getBody()).thenReturn(new JsonNode("{ " +
                "    \"status\": \"failure\",\n" +
                "    \"message\": \"Name already taken\",\n" +
                "    \"data\": {}}"));

        //TestFX
        String username = "username";
        String password = "password";

        clickOn("#tfUserName");
        write(username);

        clickOn("#pwUserPw");
        write(password);

        clickOn("#btnRegister");

        verify(restMock).register(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        Assert.assertEquals("failure", res.getBody().getObject().getString("status"));
        Assert.assertEquals("Name already taken", res.getBody().getObject().getString("message"));
        Assert.assertTrue(res.getBody().getObject().getJSONObject("data").isEmpty());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("-fx-border-color: #ff0000; -fx-border-width: 2px;", tfUserName.getStyle());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("-fx-border-color: #ff0000; -fx-border-width: 2px;", pwUserPw.getStyle());


        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getUserKey());
    }

    @Test
    public void testMissingUsername() {
        //TestFX
        String password = "password";

        clickOn("#tfUserName");
        write("");

        clickOn("#pwUserPw");
        write(password);

        clickOn("#btnRegister");

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Please type in username and password.", errorLabel.getText());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("-fx-border-color: #ff0000; -fx-border-width: 2px;", tfUserName.getStyle());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("-fx-border-color: #ff0000; -fx-border-width: 2px;", pwUserPw.getStyle());

        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getUserKey());
    }

    @Test
    public void testMissingPassword() {

        //TestFX
        String username = "username";

        clickOn("#tfUserName");
        write(username);

        clickOn("#pwUserPw");
        write("");

        clickOn("#btnRegister");

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Please type in username and password.", errorLabel.getText());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("-fx-border-color: #ff0000; -fx-border-width: 2px;", tfUserName.getStyle());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("-fx-border-color: #ff0000; -fx-border-width: 2px;", pwUserPw.getStyle());

        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getUserKey());
    }

}

