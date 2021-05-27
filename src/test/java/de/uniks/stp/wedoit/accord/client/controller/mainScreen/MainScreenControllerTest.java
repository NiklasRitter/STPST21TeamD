package de.uniks.stp.wedoit.accord.client.controller.mainScreen;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.scene.control.ListView;
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


public class MainScreenControllerTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private Stage stage;
    private StageManager stageManager;
    private LocalUser localUser;
    @Mock
    private WebSocketClient systemWebSocketClient;
    @Mock
    private WebSocketClient chatWebSocketClient;
    @Mock
    private WebSocketClient channelChatWebSocketClient;
    private Server server;

    @Mock
    private WebSocketClient webSocketClient;
    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentCaptorWebSocket;
    private WSCallback wsCallback;

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
        localUser = stageManager.getEditor().haveLocalUser("John_Doe", "testKey123");
        stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "5e2ffbd8770dd077d03df505", webSocketClient);
        stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "5e2ffbd8770dd077d03df506", webSocketClient);

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
        systemWebSocketClient = null;
        chatWebSocketClient = null;
        channelChatWebSocketClient = null;
        server = null;
        webSocketClient = null;
        restMock = null;
        res = null;
        callbackArgumentCaptor = null;
        callbackArgumentCaptorWebSocket = null;
        wsCallback = null;
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
    public void privateChatsButtonTest() {
        this.stageManager.getEditor().getNetworkController().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + this.localUser.getName(), chatWebSocketClient);

        clickOn("#btnPrivateChats");
        Assert.assertEquals("Private Chats", stage.getTitle());
    }

    @Test
    public void optionsButtonTest() {
        clickOn("#btnOptions");
        Assert.assertEquals("Options", stageManager.getPopupStage().getTitle());
    }

    // Test: list View load servers correct in the list view and sorted alphabetical
    @Test
    public void loadListViewWithTwoServersTest() {
        JsonObject json = buildGetServersSuccessWithTwoServers();

        // Mock the rest client getServers method
        mockRestClient(json);

        ListView<Server> listView = lookup("#lwServerList").queryListView();

        // Test that two servers are listed in the listView
        Assert.assertEquals(2, listView.getItems().toArray().length);
        // Test that only servers are in the list
        for (Object server : listView.getItems()) {
            Assert.assertTrue(server instanceof Server);
        }

        //Test correct alphabetical order of the items and Test correct items in the list view
        Assert.assertEquals("AMainTestServerTwo", (listView.getItems().get(0)).getName());
        Assert.assertEquals("BMainTestServerOne", (listView.getItems().get(1)).getName());

    }

    // Test: list View load zero servers correct in the list view
    @Test
    public void loadListViewWithZeroServersTest() {
        JsonObject json = buildGetServersSuccessWithZeroServers();

        // Mock the rest client getServers method
        mockRestClient(json);

        ListView<Server> listView = lookup("#lwServerList").queryListView();

        Assert.assertEquals(0, listView.getItems().toArray().length);

    }

    // Test: list view change correct with alphabetical order when a new server was created
    @Test
    public void listViewAddPropertyChangeListenerTest() {
        JsonObject json = buildGetServersSuccessWithTwoServers();

        mockRestClient(json);

        ListView<Server> listView = lookup("#lwServerList").queryListView();

        Assert.assertEquals(2, listView.getItems().toArray().length);
        for (Object server : listView.getItems()) {
            Assert.assertTrue(server instanceof Server);
        }
        Assert.assertEquals("AMainTestServerTwo", (listView.getItems().get(0)).getName());
        Assert.assertEquals("BMainTestServerOne", (listView.getItems().get(1)).getName());

        //create a new server
        stageManager.getEditor().haveServer(stageManager.getEditor().getLocalUser(), "123", "AOServer");

        WaitForAsyncUtils.waitForFxEvents();

        // Test count of servers
        Assert.assertEquals(3, listView.getItems().toArray().length);
        for (Object server : listView.getItems()) {
            Assert.assertTrue(server instanceof Server);
        }

        //Test correct alphabetical order of the items and Test correct items in the list view
        Assert.assertEquals("AMainTestServerTwo", listView.getItems().get(0).getName());
        Assert.assertEquals("AOServer", listView.getItems().get(1).getName());
        Assert.assertEquals("BMainTestServerOne", listView.getItems().get(2).getName());
    }

    // Test getServer failure message handling, server show LoginScreen
    @Test
    public void failureMessageTest() {
        JsonObject json = buildGetServersFailureResponse();

        mockRestClient(json);

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Login", stage.getTitle());
    }

    // Test open server with a double click on this one
    @Test
    public void openServerDoubleClickedTest() {

        JsonObject json = buildGetServersSuccessWithTwoServers();

        mockRestClient(json);

        ListView<Server> listView = lookup("#lwServerList").queryListView();

        Assert.assertEquals(2, listView.getItems().toArray().length);
        for (Object server : listView.getItems()) {
            Assert.assertTrue(server instanceof Server);
        }
        Assert.assertEquals("AMainTestServerTwo", (listView.getItems().get(0)).getName());
        Assert.assertEquals("BMainTestServerOne", (listView.getItems().get(1)).getName());

        // Select server one
        listView.getSelectionModel().select(1);
        Server server = listView.getSelectionModel().getSelectedItem();

        stageManager.getEditor().getNetworkController().haveWebSocket(CHAT_USER_URL + this.localUser.getName()
                + AND_SERVER_ID_URL + server.getId(), channelChatWebSocketClient);
        WaitForAsyncUtils.async(() -> {
            while (!stage.getTitle().equals("Server")) {
                stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "5e2ffbd8770dd077d03df505", webSocketClient);
            }
        });
        doubleClickOn("#lwServerList");
        WaitForAsyncUtils.waitForFxEvents();
        // Test correct server and correct screen
        Assert.assertEquals("BMainTestServerOne", server.getName());
        Assert.assertEquals("Server", stage.getTitle());
    }

    public void mockWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(webSocketClient, times(2)).setCallback(callbackArgumentCaptorWebSocket.capture());
        this.wsCallback = callbackArgumentCaptorWebSocket.getValue();

        this.wsCallback.handleMessage(webSocketJson);
    }

    @Test
    public void handleServerMessage() {
        ListView<Server> listView = lookup("#lwServerList").queryListView();
        Assert.assertNotNull(listView);

        mockRestClient(buildGetServersSuccessWithTwoServers());
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(2, listView.getItems().size());


        Server setServer = null;
        for (Server server : listView.getItems()) {
            if (server.getId().equals(webSocketCallbackServerUpdated().getJsonObject(DATA).getString(ID))) {
                setServer = server;
            }
        }
        Assert.assertNotNull(setServer);
        Assert.assertNotEquals(setServer.getName(), webSocketCallbackServerUpdated().getJsonObject(DATA).getString(NAME));


        mockWebSocket(webSocketCallbackServerUpdated());
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(setServer.getName(), webSocketCallbackServerUpdated().getJsonObject(DATA).getString(NAME));
        Assert.assertEquals(2, listView.getItems().size());

        mockWebSocket(webSocketCallbackServerDeleted());
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertFalse(listView.getItems().contains(setServer));
        Assert.assertEquals(1, listView.getItems().size());
    }

    // Help methods to create response for mocked rest client

    public JsonObject webSocketCallbackServerUpdated() {
        return Json.createObjectBuilder().add("action", "serverUpdated").add("data",
                Json.createObjectBuilder().add("id", "5e2ffbd8770dd077d03df505").add("name", "serverUpdated")).build();
    }

    public JsonObject webSocketCallbackServerDeleted() {
        return Json.createObjectBuilder().add("action", "serverDeleted").add("data",
                Json.createObjectBuilder().add("id", "5e2ffbd8770dd077d03df505").add("name", "serverUpdated")).build();
    }

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
