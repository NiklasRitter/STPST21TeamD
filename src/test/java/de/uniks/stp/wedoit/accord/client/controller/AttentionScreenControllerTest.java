package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.scene.control.Label;
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

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AttentionScreenControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager stageManager;
    private LocalUser localUser;
    private Server server;

    @Mock
    private RestClient restMock;
    @Mock
    private HttpResponse<JsonNode> res;
    @Mock
    WebSocketClient webSocketClientMock;
    @Mock
    WebSocketClient chatWebSocketClientMock;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

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
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);

        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = stageManager.getEditor().haveLocalUser("Alice", "userKey123").setId("owner123");
        this.server = stageManager.getEditor().haveServer(localUser, "id456", "AliceServer");
        this.stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClientMock);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(CHAT_USER_URL + stageManager.getEditor().getNetworkController().getCleanLocalUserName()
                + AND_SERVER_ID_URL + this.server.getId(), chatWebSocketClientMock);

        this.stageManager.getEditor().getNetworkController().setRestClient(restMock);

        // first have to show server screen, so that the members and so on are loaded correctly
        StageManager.showServerScreen(server);
        StageManager.showEditServerScreen(server);
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Mock the rest client's getServers method and create a callback
     *
     * @param restClientJson JsonObject, which one should return from the rest client as JsonNode
     */
    public void mockRestExplicitServer(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock, atLeastOnce()).getExplicitServerInformation(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    private void mockRestDeleteServer(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock, atLeastOnce()).deleteServer(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    @Test
    public void deleteServerSuccessful() {
        JsonObject serverJson = buildServerInformationWithOneMember();
        mockRestExplicitServer(serverJson);

        clickOn("#btnDelete");

        Assert.assertEquals(stageManager.getPopupStage().getTitle(), "Attention");
        Assert.assertEquals(localUser.getServers().size(), 1);

        clickOn("#btnDelete");

        JsonObject jsonObject = buildDeleteServerSuccessful(server);
        mockRestDeleteServer(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(stageManager.getStage().getTitle(), "Main");
        Assert.assertEquals(localUser.getServers().size(), 0);
    }

    @Test
    public void deleteServerFailure() {
        JsonObject serverJson = buildServerInformationWithOneMember();
        mockRestExplicitServer(serverJson);

        clickOn("#btnDelete");

        Assert.assertEquals(stageManager.getPopupStage().getTitle(), "Attention");
        Assert.assertEquals(localUser.getServers().size(), 1);

        clickOn("#btnDelete");

        JsonObject jsonObject = buildDeleteServerFailure();
        mockRestDeleteServer(jsonObject);

        WaitForAsyncUtils.waitForFxEvents();

        Label lblError = (Label) lookup("#lblError").query();
        Assert.assertEquals(stageManager.getPopupStage().getTitle(), "Attention");
        Assert.assertTrue(lblError.isVisible());
        Assert.assertEquals(lblError.getText(), "Error. Delete Server was not successful!");
        Assert.assertEquals(localUser.getServers().size(), 1);
    }

    @Test
    public void discardDeleteServer() {
        JsonObject serverJson = buildServerInformationWithOneMember();
        mockRestExplicitServer(serverJson);

        clickOn("#btnDelete");

        Assert.assertEquals(stageManager.getPopupStage().getTitle(), "Attention");
        Assert.assertEquals(localUser.getServers().size(), 1);

        clickOn("#btnDiscard");

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(stageManager.getPopupStage().getTitle(), "Edit Server");
        Assert.assertEquals(localUser.getServers().size(), 1);
    }

    private JsonObject buildDeleteServerSuccessful(Server server) {
        return Json.createObjectBuilder().add(STATUS, SUCCESS)
                .add(MESSAGE, "")
                .add(DATA, Json.createObjectBuilder()
                        .add(ID, server.getId())
                        .add(NAME, server.getName()))
                .build();
    }

    private JsonObject buildDeleteServerFailure() {
        return Json.createObjectBuilder().add(STATUS, FAILURE)
                .add(MESSAGE, "Something went wrong!")
                .add(DATA, Json.createObjectBuilder())
                .build();
    }

    public JsonObject buildServerInformationWithOneMember() {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", server.getId())
                        .add("name", server.getName())
                        .add("owner", "owner123")
                        .add("categories", Json.createArrayBuilder())
                        .add("members", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("id", "owner123").add("name", "Alice")
                                        .add("online", true))))
                .build();
    }

    @Override
    public void stop() {
        stage = null;
        stageManager = null;
        localUser = null;
        server = null;
        restMock = null;
        res = null;
        webSocketClientMock = null;
        chatWebSocketClientMock = null;
        callbackArgumentCaptor = null;
        rule = null;
    }
}
