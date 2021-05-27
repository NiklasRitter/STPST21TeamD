package de.uniks.stp.wedoit.accord.client.controller.createServerScreen;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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

import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateServerScreenControllerTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private Stage stage;
    private StageManager stageManager;
    private LocalUser localUser;
    @Mock
    private WebSocketClient webSocketClient;
    @Mock
    private WebSocketClient channelChatWebSocketClient;
    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

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
        this.stageManager.start(stage);

        //create localUser to skip the login screen
        localUser = stageManager.getEditor().haveLocalUser("John", "testKey123");
        stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "5e2ffbd8770dd077d03df505", webSocketClient);
        stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "5e2ffbd8770dd077d03df506", webSocketClient);

        stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "123", webSocketClient);

        this.stageManager.getEditor().getNetworkController().setRestClient(restMock);
        StageManager.showMainScreen();
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() {
        rule = null;
        stage = null;
        stageManager = null;
        localUser = null;
        webSocketClient = null;
        channelChatWebSocketClient = null;
        restMock = null;
        res = null;
        callbackArgumentCaptor = null;
    }

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
    public void createServerTestSuccessful() {
        JsonObject json = buildGetServersSuccessWithTwoServers();

        // Mock the rest client getServers method
        mockRestClient(json);
        WaitForAsyncUtils.waitForFxEvents();
        ListView<Server> listView = lookup("#lwServerList").queryListView();

        Assert.assertEquals(2, listView.getItems().toArray().length);
        Assert.assertEquals("AMainTestServerTwo", (listView.getItems().get(0)).getName());
        Assert.assertEquals("BMainTestServerOne", (listView.getItems().get(1)).getName());

        clickOn("#btnAddServer");

        Assert.assertEquals("Create Server", stageManager.getPopupStage().getTitle());

        TextField serverTextField = lookup("#tfServerName").query();
        String serverName = "MySuperServer";
        serverTextField.setText(serverName);

        when(res.getBody()).thenReturn(new JsonNode(buildCreateServer(serverName).toString()));

        clickOn("#btnCreateServer");

        verify(restMock).createServer(anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        Server server = null;
        for (Server serverIterator : localUser.getServers()) {
            if (serverIterator.getName().equals(serverName)) {
                server = serverIterator;
            }
        }
        Assert.assertNotNull(server);

        stageManager.getEditor().getNetworkController().haveWebSocket(CHAT_USER_URL + this.localUser.getName()
                + AND_SERVER_ID_URL + server.getId(), channelChatWebSocketClient);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("Server", stage.getTitle());
    }

    @Test
    public void createServerFailure() {
        JsonObject json = buildGetServersSuccessWithTwoServers();

        // Mock the rest client getServers method
        mockRestClient(json);
        WaitForAsyncUtils.waitForFxEvents();
        ListView<Server> listView = lookup("#lwServerList").queryListView();

        Assert.assertEquals(2, listView.getItems().toArray().length);
        Assert.assertEquals("AMainTestServerTwo", (listView.getItems().get(0)).getName());
        Assert.assertEquals("BMainTestServerOne", (listView.getItems().get(1)).getName());

        clickOn("#btnAddServer");

        Assert.assertEquals("Create Server", stageManager.getPopupStage().getTitle());

        TextField serverTextField = lookup("#tfServerName").query();
        String serverName = "MySuperServer";
        serverTextField.setText(serverName);

        when(res.getBody()).thenReturn(new JsonNode(buildCreateServerFailure().toString()));

        clickOn("#btnCreateServer");

        verify(restMock).createServer(anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        Server server = null;
        for (Server serverIterator : localUser.getServers()) {
            if (serverIterator.getName().equals(serverName)) {
                server = serverIterator;
            }
        }
        Assert.assertNull(server);

        TextField textField = lookup("#tfServerName").query();
        Assert.assertEquals("text-input text-field error", textField.getStyleClass().toString());

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Something went wrong while creating the server", errorLabel.getText());

    }

    @Test
    public void createServerTooShortName() {
        JsonObject json = buildGetServersSuccessWithTwoServers();

        // Mock the rest client getServers method
        mockRestClient(json);
        WaitForAsyncUtils.waitForFxEvents();
        ListView<Server> listView = lookup("#lwServerList").queryListView();

        Assert.assertEquals(2, listView.getItems().toArray().length);
        Assert.assertEquals("AMainTestServerTwo", (listView.getItems().get(0)).getName());
        Assert.assertEquals("BMainTestServerOne", (listView.getItems().get(1)).getName());

        clickOn("#btnAddServer");

        Assert.assertEquals("Create Server", stageManager.getPopupStage().getTitle());

        TextField serverTextField = lookup("#tfServerName").query();
        String serverName = "";
        serverTextField.setText(serverName);

        clickOn("#btnCreateServer");

        WaitForAsyncUtils.waitForFxEvents();

        Server server = null;
        for (Server serverIterator : localUser.getServers()) {
            if (serverIterator.getName().equals(serverName)) {
                server = serverIterator;
            }
        }
        Assert.assertNull(server);
        TextField textField = lookup("#tfServerName").query();
        Assert.assertEquals("text-input text-field error", textField.getStyleClass().toString());

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Name has to be at least 1 symbols long", errorLabel.getText());
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
     * create response when a server created
     */
    public JsonObject buildCreateServer(String name) {
        return Json.createObjectBuilder().add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", "123")
                        .add("name", name)).build();
    }

    /**
     * create createServer-response when create server failed
     */
    public JsonObject buildCreateServerFailure() {
        return Json.createObjectBuilder().add("status", "failure")
                .add("message", "")
                .add("data", Json.createObjectBuilder()).build();
    }


}
