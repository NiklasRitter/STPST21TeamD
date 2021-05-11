package de.uniks.stp.wedoit.accord.client.controller.welcomeScreen;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class onlineUserListViewTest extends ApplicationTest {

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
    private WebSocketClient socketMock;

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

    public void directToWelcomeScreen() {
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

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnWelcome");
    }

    @Test
    public void verifyCallToIsOpenConnection() {
     /*   WebSocketSession session = mock(WebSocketSession.class);
        TextMessage textMsg = new TextMessage("Test Message".getBytes());

        when(session.isOpen()).thenReturn(true);

        TextHandler textHandler = new TextHandler();

        // Pass the mocked session object here
        textHandler.handleTextMessage(session, textMsg);

        // Now you can verify if session.sendMessage() was called or not
        verify(session, times(1)).sendMessage(textMsg);*/
    }

}
