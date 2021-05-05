package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
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

import javax.json.Json;
import javax.json.JsonObject;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class MainScreenControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager stageManager;
    private LocalUser localUser;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);

        //create localUser to skip the login screen
        localUser = stageManager.getEditor().haveLocalUser("John Doe", "testKey123");

        this.stageManager.showMainScreen(restMock);
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Mock the rest client's getServers method and create a callback
     *
     * @param json JsonObject, which one should return from the rest client as JsonNode
     */
    public void mockRestClient(JsonObject json) {
        when(res.getBody()).thenReturn(new JsonNode(json.toString()));

        verify(restMock).getServers(anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    @Test
    public void welcomeButtonTest() {
        clickOn("#btnWelcome");
        Assert.assertEquals("Welcome", stage.getTitle());
    }

    @Test
    public void optionsButtonTest() {
        clickOn("#btnOptions");
        Assert.assertEquals("Options", stage.getTitle());
    }

    // Help methods to create response for mocked rest client

    /**
     * create a getServers response with two servers with id an name:
     * <b>{"id":"5e2ffbd8770dd077d03df505","name":"BMainTestServerOne"},
     * {"id":"5e2ffbd8770dd077d03df506","name":"AMainTestServerTwo"}</b>
     */
    public JsonObject buildGetServersSuccessWithTwoServers() {
        return Json.createObjectBuilder()
                .add("status", "success").add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df505")
                                .add("name", "BMainTestServerOne")
                        )
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df506")
                                .add("name", "AMainTestServerTwo"))
                ).build();
    }

    /**
     * create a getServers response with zero servers
     */
    public JsonObject buildGetServersSuccessWithZeroServers() {
        return Json.createObjectBuilder()
                .add("status", "success").add("message", "")
                .add("data", Json.createArrayBuilder()
                ).build();
    }

    /**
     * create a getServers response with a failure status
     */
    public JsonObject buildGetServersFailureResponse() {
        return Json.createObjectBuilder()
                .add("status", "failure").add("message", "Log in first")
                .add("data", Json.createObjectBuilder()
                ).build();
    }

}
