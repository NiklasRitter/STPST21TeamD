package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
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

public class WelcomeScreenTest  extends ApplicationTest {

    private Stage stage;
    private StageManager stageManager;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);
        this.stageManager.showLoginScreen(restMock);
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> resLogout;

    @Mock
    private HttpResponse<JsonNode> resLogin;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptorLogout;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptorLogin;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    public void directToWelcomeScreen() {
        //Mocking of RestClient login function
        when(resLogin.getBody()).thenReturn(new JsonNode("{" +
                "\"status\": \"success\",\n" +
                "\"message\": \"\",\n" +
                "\"data\": {\n" +
                "\"userKey\": \"c653b568-d987-4331-8d62-26ae617847bf\"\n" +
                " }" +
                "}"));

        //TestFX
        String username = "username";
        String password = "password";

        clickOn("#tfUserName");
        write(username);

        clickOn("#pwUserPw");
        write(password);

        clickOn("#btnLogin");

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptorLogin.capture());

        Callback<JsonNode> callbackLogin = callbackArgumentCaptorLogin.getValue();
        callbackLogin.completed(resLogin);

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnWelcome");

    }

    @Test
    public void testBtnLogout() {

        when(resLogout.getBody()).thenReturn(new JsonNode("{" +
                "\"status\": \"success\",\n" +
                "\"message\": \"Logged out\",\n" +
                "\"data\": \"{}\" " +
                "}"));

        directToWelcomeScreen();

        // got to welcome screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Welcome", stage.getTitle());

        // testing logout button
        clickOn("#btnLogout");

        verify(restMock).logout(anyString(), callbackArgumentCaptorLogout.capture());

        Callback<JsonNode> callbackLogout = callbackArgumentCaptorLogout.getValue();
        callbackLogout.completed(resLogout);

        Assert.assertEquals("success", resLogout.getBody().getObject().getString("status"));
        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getUserKey());

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Login", stage.getTitle());
    }

    @Test
    public void testBtnOptions() {

        directToWelcomeScreen();

        // got to welcome screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Welcome", stage.getTitle());

        // testing options button
        clickOn("#btnOptions");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Options", stage.getTitle());
    }

    @Test
    public void testBtnHome() {

        directToWelcomeScreen();

        // got to welcome screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Welcome", stage.getTitle());

        // testing home button
        clickOn("#btnHome");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Main", stage.getTitle());
    }
}
