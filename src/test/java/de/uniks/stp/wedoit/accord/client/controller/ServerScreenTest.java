package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
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
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * tests for the ServerScreenController
 * - user list view test
 * - logout test
 * - channel tree view test
 */
public class ServerScreenTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    WebSocketClient webSocketClient;
    @Mock
    WebSocketClient chatWebSocketClient;

    private Stage stage;
    private Stage popupStage;
    private StageManager stageManager;
    private LocalUser localUser;
    private Server server;
    @Mock
    private RestClient restMock;
    @Mock
    private HttpResponse<JsonNode> res;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> channelsCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> channelCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> categoriesCallbackArgumentCaptor;


    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentCaptorWebSocket;
    private WSCallback wsCallback;

    @Captor
    private ArgumentCaptor<WSCallback> chatCallbackArgumentCaptorWebSocket;

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
        this.popupStage = this.stageManager.getPopupStage();
        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = stageManager.getEditor().haveLocalUser("John_Doe", "testKey123");
        this.localUser.setId("123");
        this.server = stageManager.getEditor().haveServer(localUser, "testId", "TServer");
        this.stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClient);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(CHAT_USER_URL + stageManager.getEditor().
                getNetworkController().getCleanLocalUserName() + AND_SERVER_ID_URL + this.server.getId(), chatWebSocketClient);

        this.stageManager.getEditor().getNetworkController().setRestClient(restMock);
        StageManager.showServerScreen(server);

        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() {
        rule = null;
        webSocketClient = null;
        chatWebSocketClient = null;
        stage = null;
        popupStage = null;
        stageManager = null;
        localUser = null;
        server = null;
        restMock = null;
        res = null;
        callbackArgumentCaptor = null;
        channelsCallbackArgumentCaptor = null;
        channelCallbackArgumentCaptor = null;
        categoriesCallbackArgumentCaptor = null;
        callbackArgumentCaptorWebSocket = null;
        wsCallback = null;
        chatCallbackArgumentCaptorWebSocket = null;
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    public void mockRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).getExplicitServerInformation(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(webSocketClient).setCallback(callbackArgumentCaptorWebSocket.capture());
        this.wsCallback = callbackArgumentCaptorWebSocket.getValue();

        this.wsCallback.handleMessage(webSocketJson);
    }

    public void mockChannelRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).getChannels(anyString(), anyString(), anyString(), channelCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = channelCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockGetCategoryRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).getCategories(anyString(), anyString(), categoriesCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = categoriesCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockCreateCategoryRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).createCategory(anyString(), anyString(), anyString(), categoriesCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = categoriesCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockCreateChannelRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

<<<<<<< HEAD
        verify(restMock).createChannel(anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString(), channelsCallbackArgumentCaptor.capture());
=======
        verify(restMock).createChannel(anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString(), categoriesCallbackArgumentCaptor.capture());
>>>>>>> TD21-58

        Callback<JsonNode> callback = channelsCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockChatWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(chatWebSocketClient, atLeastOnce()).setCallback(chatCallbackArgumentCaptorWebSocket.capture());
        WSCallback chatWsCallback = chatCallbackArgumentCaptorWebSocket.getValue();

        chatWsCallback.handleMessage(webSocketJson);
    }

    @Test
    public void initUserListView() {
        JsonObject restJson = getServerIdSuccessful();
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        ListView<Object> listView = lookup("#lvServerUsers").queryListView();
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertEquals(0, listView.getItems().toArray().length);
        mockRest(restJson);
        Assert.assertEquals(3, listView.getItems().toArray().length);
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(0)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(1)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(2)));
        Assert.assertFalse(listView.getItems().contains(new Server()));

        mockWebSocket(webSocketJson);
    }

    private void initChannelListView() {
        JsonObject categoriesRestJson = getServerCategories();
        mockGetCategoryRest(categoriesRestJson);
        JsonObject channelRestJson = getCategoryChannels();
        mockChannelRest(channelRestJson);
    }

    public void initChannelListViewChannelFailure() {
        JsonObject categoriesRestJson = getServerCategories();
        mockGetCategoryRest(categoriesRestJson);
        JsonObject channelRestJson = getCategoryChannelsFailure();
        mockChannelRest(channelRestJson);
    }

    public void initChannelListViewCategoryFailure() {
        JsonObject categoriesRestJson = getServerCategoriesFailure();
        mockGetCategoryRest(categoriesRestJson);
    }

    @Test
    public void updateUserListView() {
        JsonObject restJson = getServerIdSuccessful();
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        ListView<Object> listView = lookup("#lvServerUsers").queryListView();
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertEquals(0, listView.getItems().toArray().length);
        mockRest(restJson);
        Assert.assertEquals(3, listView.getItems().toArray().length);
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(0)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(1)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(2)));
        Assert.assertFalse(listView.getItems().contains(new Server()));


        mockWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(4, listView.getItems().toArray().length);
        Assert.assertEquals(4, server.getMembers().toArray().length);
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(0)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(1)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(2)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(3)));
        Assert.assertFalse(listView.getItems().contains(new Server()));
        User userPhil = null;
        User userI2 = null;
        for (Object user : listView.getItems()) {
            if (user instanceof User) {
                if (((User) user).getName().equals("Phil")) {
                    userPhil = (User) user;
                }
                if (((User) user).getName().equals("N2")) {
                    userI2 = (User) user;
                }
            }
        }
        Assert.assertNotNull(userPhil);
        Assert.assertNotNull(userI2);
        Assert.assertTrue(userPhil.isOnlineStatus());
        Assert.assertFalse(userI2.isOnlineStatus());

        wsCallback.handleMessage(webSocketCallbackUserLeft());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertFalse(userPhil.isOnlineStatus());
        Assert.assertFalse(userI2.isOnlineStatus());

    }

    @Test
    public void restClientFailureResponse() {
        JsonObject restJson = getServerIdFailure();
        ListView<Object> listView = lookup("#lvServerUsers").queryListView();
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertEquals(0, listView.getItems().toArray().length);
        mockRest(restJson);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Login", stage.getTitle());
    }

    @Test
    public void LogoutSuccessfulTest() {
        Assert.assertEquals("Server", stage.getTitle());
        mockRest(getServerIdSuccessful());
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(logoutSuccessful().toString()));
        clickOn("#btnLogout");
        verify(restMock).logout(anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Login", stage.getTitle());


    }

    @Test
    public void logoutFailureTest() {
        Assert.assertEquals("Server", stage.getTitle());
        mockRest(getServerIdSuccessful());
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(logoutFailure().toString()));
        clickOn("#btnLogout");
        verify(restMock).logout(anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Login", stage.getTitle());


    }

    @Test
    public void mainScreenButtonTest() {
        clickOn("#btnHome");
        Assert.assertEquals("Main", stage.getTitle());
    }

    @Test
    public void optionsButtonTest() {
        clickOn("#btnOptions");
        Assert.assertEquals("Options", stageManager.getPopupStage().getTitle());
    }

    @Test
    public void initChannelsTest() {
        JsonObject restJson = getServerIdSuccessful();
        ListView<Object> listView = lookup("#lvServerUsers").queryListView();
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertEquals(0, listView.getItems().toArray().length);
        mockRest(restJson);
        Assert.assertEquals(3, listView.getItems().toArray().length);
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(0)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(1)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(2)));
        Assert.assertFalse(listView.getItems().contains(new Server()));

        when(res.getBody()).thenReturn(new JsonNode(getCategories().toString()));
        verify(restMock).getCategories(anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> catCallback = callbackArgumentCaptor.getValue();
        catCallback.completed(res);

        when(res.getBody()).thenReturn(new JsonNode(getChannels().toString()));

        verify(restMock, atLeastOnce()).getChannels(anyString(), anyString(), anyString(), channelsCallbackArgumentCaptor.capture());
        List<Callback<JsonNode>> channelCallbacks = channelsCallbackArgumentCaptor.getAllValues();

        for (Callback<JsonNode> callback : channelCallbacks
        ) {
            callback.completed(res);
        }

        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();
        WaitForAsyncUtils.waitForFxEvents();
        TreeItem<Object> treeRoot = tvServerChannels.getRoot();
        Category categoryOne = (Category) treeRoot.getChildren().get(0).getValue();
        Category categoryTwo = (Category) treeRoot.getChildren().get(1).getValue();
        Category categoryThree = (Category) treeRoot.getChildren().get(2).getValue();
        Assert.assertEquals("Category1", categoryOne.getName());
        Assert.assertEquals("Category2", categoryTwo.getName());
        Assert.assertEquals("Category3", categoryThree.getName());

        Assert.assertEquals(3, categoryOne.getChannels().size());
        Assert.assertEquals(3, categoryTwo.getChannels().size());
        Assert.assertEquals(3, categoryThree.getChannels().size());

    }


    @Test
    public void sendChatMessageTest() {
        //init channel list and select first channel
        initUserListView();
        initChannelListView();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        Button btnEmoji = lookup("#btnEmoji").query();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();

        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());

        clickOn("#btnEmoji");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(popupStage.isShowing());
        Assert.assertEquals("Emoji Picker", popupStage.getTitle());

        GridPane panelForEmojis = (GridPane) popupStage.getScene().getRoot().lookup("#panelForEmojis");
        EmojiButton emoji = (EmojiButton) panelForEmojis.getChildren().get(0);
        clickOn(emoji);

        //send message
        clickOn("#tfInputMessage");
        write("Test Message");
        press(KeyCode.ENTER);

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "Test Message" + emoji.getText());
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lvTextChat.getItems().size());
        Assert.assertEquals(channel.getMessages().size(), lvTextChat.getItems().size());
        Assert.assertEquals(lvTextChat.getItems().get(0), channel.getMessages().get(0));
        Assert.assertEquals(lvTextChat.getItems().get(0).getText(), channel.getMessages().get(0).getText());
        Assert.assertEquals("Test Message" + emoji.getText(), lvTextChat.getItems().get(0).getText());
    }

    @Test
    public void getCategoryChannelsFailureTest() {
        //init channel list and select first channel
        initUserListView();
        initChannelListViewChannelFailure();

        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();
        WaitForAsyncUtils.waitForFxEvents();
        TreeItem<Object> root = tvServerChannels.getRoot();
        for (Object category : root.getChildren()) {
            Assert.assertTrue(category instanceof TreeItem);
            Assert.assertEquals(0, ((TreeItem<?>) category).getChildren().size());
        }

    }

    @Test
    public void getServerCategoriesFailureTest() {
        //init channel list and select first channel
        initUserListView();
        initChannelListViewCategoryFailure();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();
        WaitForAsyncUtils.waitForFxEvents();
        TreeItem<Object> root = tvServerChannels.getRoot();
        Assert.assertEquals(0, root.getChildren().size());

    }

    @Test
    public void testChatMessagesCachedProperlyAfterChannelChange() {
        //init user list and select first user
        initUserListView();
        initChannelListView();
        Label lbChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();

        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        TreeItem<Object> channelItem = tvServerChannels.getRoot().getChildren().get(0).getChildren().get(0);
        Channel channel = (Channel) channelItem.getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lbChannelName.getText());

        //send message
        ((TextField) lookup("#tfInputMessage").query()).setText("Test Message");
        clickOn("#tfInputMessage");
        press(KeyCode.ENTER);

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lvTextChat.getItems().size());
        Assert.assertEquals(channel.getMessages().size(), lvTextChat.getItems().size());
        Assert.assertEquals(lvTextChat.getItems().get(0), channel.getMessages().get(0));
        Assert.assertEquals(lvTextChat.getItems().get(0).getText(), channel.getMessages().get(0).getText());
        Assert.assertEquals("Test Message", lvTextChat.getItems().get(0).getText());

        tvServerChannels.getSelectionModel().select(2);
        channelItem = tvServerChannels.getRoot().getChildren().get(0).getChildren().get(1);
        Channel channel1 = (Channel) channelItem.getValue();

        clickOn("#tvServerChannels");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel1.getName(), lbChannelName.getText());

        Assert.assertEquals(0, lvTextChat.getItems().size());

        clickOn("#tfInputMessage");

        tvServerChannels.getSelectionModel().select(1);
        channelItem = tvServerChannels.getRoot().getChildren().get(0).getChildren().get(0);
        Channel channel2 = (Channel) channelItem.getValue();
        clickOn("#tvServerChannels");

        Assert.assertEquals(channel2.getName(), lbChannelName.getText());

        Assert.assertEquals(1, lvTextChat.getItems().size());
        Assert.assertEquals(channel2.getMessages().size(), lvTextChat.getItems().size());
        Assert.assertEquals(lvTextChat.getItems().get(0), channel2.getMessages().get(0));
        Assert.assertEquals(lvTextChat.getItems().get(0).getText(), channel2.getMessages().get(0).getText());
        Assert.assertEquals(lvTextChat.getItems().get(0).getText(), "Test Message");
    }


    @Test
    public void handleServerMessages() {
        initUserListView();
        initChannelListView();

        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();

        WaitForAsyncUtils.waitForFxEvents();

        mockWebSocket(webSocketCallbackCategoryCreated());
        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(3);
        Assert.assertNotNull(tvServerChannels.getSelectionModel().getSelectedItem());
        Assert.assertTrue(tvServerChannels.getSelectionModel().getSelectedItem().getValue() instanceof Category);
        Category category = (Category) tvServerChannels.getSelectionModel().getSelectedItem().getValue();
        Assert.assertEquals(category.getName(), webSocketCallbackCategoryCreated().getJsonObject(DATA).getString(NAME));
        Assert.assertEquals(category.getId(), webSocketCallbackCategoryCreated().getJsonObject(DATA).getString(ID));
        Assert.assertEquals(category.getServer().getCategories().size(), 2);

        mockWebSocket(webSocketCallbackCategoryUpdated());
        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(null);
        tvServerChannels.getSelectionModel().select(3);
        Assert.assertNotNull(tvServerChannels.getSelectionModel().getSelectedItem());
        Assert.assertTrue(tvServerChannels.getSelectionModel().getSelectedItem().getValue() instanceof Category);
        category = (Category) tvServerChannels.getSelectionModel().getSelectedItem().getValue();
        Assert.assertNotEquals(category.getName(), webSocketCallbackCategoryCreated().getJsonObject(DATA).getString(NAME));
        Assert.assertEquals(category.getName(), webSocketCallbackCategoryUpdated().getJsonObject(DATA).getString(NAME));
        Assert.assertEquals(category.getId(), webSocketCallbackCategoryUpdated().getJsonObject(DATA).getString(ID));
        Assert.assertEquals(category.getServer().getCategories().size(), 2);

        mockWebSocket(webSocketCallbackChannelCreated());
        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(null);
        tvServerChannels.getSelectionModel().select(4);
        Assert.assertNotNull(tvServerChannels.getSelectionModel().getSelectedItem());
        Assert.assertTrue(tvServerChannels.getSelectionModel().getSelectedItem().getValue() instanceof Channel);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();
        Assert.assertEquals(channel.getName(), webSocketCallbackChannelCreated().getJsonObject(DATA).getString(NAME));
        Assert.assertEquals(channel.getId(), webSocketCallbackChannelCreated().getJsonObject(DATA).getString(ID));
        Assert.assertEquals(category.getServer().getCategories().size(), 2);
        Assert.assertEquals(category.getChannels().size(), 1);

        mockWebSocket(webSocketCallbackChannelUpdated());
        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(null);
        tvServerChannels.getSelectionModel().select(4);
        Assert.assertNotNull(tvServerChannels.getSelectionModel().getSelectedItem());
        Assert.assertTrue(tvServerChannels.getSelectionModel().getSelectedItem().getValue() instanceof Channel);
        channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();
        Assert.assertNotEquals(channel.getName(), webSocketCallbackChannelCreated().getJsonObject(DATA).getString(NAME));
        Assert.assertEquals(channel.getName(), webSocketCallbackChannelUpdated().getJsonObject(DATA).getString(NAME));
        Assert.assertEquals(channel.getId(), webSocketCallbackChannelUpdated().getJsonObject(DATA).getString(ID));
        Assert.assertEquals(category.getServer().getCategories().size(), 2);
        Assert.assertEquals(category.getChannels().size(), 1);

        mockWebSocket(webSocketCallbackChannelCreated());
        WaitForAsyncUtils.waitForFxEvents();
        mockWebSocket(webSocketCallbackChannelDeleted());
        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(null);
        tvServerChannels.getSelectionModel().select(4);
        Assert.assertNull(tvServerChannels.getSelectionModel().getSelectedItem());
        Assert.assertEquals(category.getChannels().size(), 0);
        Assert.assertEquals(category.getServer().getCategories().size(), 2);

        Server server = category.getServer();

        mockWebSocket(webSocketCallbackCategoryDeleted());
        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(null);
        tvServerChannels.getSelectionModel().select(3);
        Assert.assertNull(tvServerChannels.getSelectionModel().getSelectedItem());
        Assert.assertEquals(server.getCategories().size(), 1);


        ListView<Object> listView = lookup("#lvServerUsers").queryListView();
        Assert.assertNotNull(listView);
        Assert.assertEquals(4, listView.getItems().size());

        mockWebSocket(webSocketCallbackUserArrived());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(5, listView.getItems().size());
        User setUser = null;
        User phil = null;
        for (User user : server.getMembers()) {
            if (user.getId().equals(webSocketCallbackUserArrived().getJsonObject(DATA).getString(ID))) {
                setUser = user;
            }
            if (user.getName().equals(webSocketCallbackUserExited().getJsonObject(DATA).getString(NAME))) {
                phil = user;
            }
        }
        Assert.assertNotNull(setUser);
        Assert.assertNotNull(phil);
        Assert.assertNotEquals(setUser.getId(), phil.getId());
        Assert.assertEquals(setUser.getName(), webSocketCallbackUserArrived().getJsonObject(DATA).getString(NAME));
        Assert.assertTrue(listView.getItems().contains(setUser));

        mockWebSocket(webSocketCallbackUserExited());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(4, listView.getItems().size());
        Assert.assertFalse(listView.getItems().contains(phil));
        Assert.assertFalse(server.getMembers().contains(phil));


        Label labelServerName = lookup("#lbServerName").query();
        Assert.assertNotNull(labelServerName);
        Assert.assertNotEquals(server.getName(), webSocketCallbackServerUpdated().getJsonObject(DATA).getString(NAME));
        Assert.assertEquals(labelServerName.getText(), server.getName());

        mockWebSocket(webSocketCallbackServerUpdated());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(labelServerName.getText(), webSocketCallbackServerUpdated().getJsonObject(DATA).getString(NAME));
        Assert.assertEquals(server.getName(), webSocketCallbackServerUpdated().getJsonObject(DATA).getString(NAME));

        mockWebSocket(webSocketCallbackServerDeleted());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Main", stage.getTitle());

    }

    @Test
    public void createCategoryTest() {
        Platform.runLater(StageManager::showCreateCategoryScreen);
        WaitForAsyncUtils.waitForFxEvents();
        Button button = (Button) lookup("#btnCreateCategory").query();
        Assert.assertEquals(button.getText(), "Create");

        TextField textField = (TextField) lookup("#tfCategoryName").query();
        textField.setText("testCategory");
        clickOn("#btnCreateCategory");

        JsonObject json = buildCreateCategory("123", "testCategory", this.server.getId());
        mockCreateCategoryRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Category newCategory = null;
        for (Category cat : this.server.getCategories()) {
            if (cat.getId().equals("123")) {
                newCategory = cat;
            }
        }
        Assert.assertNotNull(newCategory);
        Assert.assertEquals(newCategory.getName(), "testCategory");
    }

    @Test
    public void createCategoryFailureTest() {
        Platform.runLater(StageManager::showCreateCategoryScreen);
        WaitForAsyncUtils.waitForFxEvents();
        Button button = (Button) lookup("#btnCreateCategory").query();
        Assert.assertEquals(button.getText(), "Create");

        clickOn("#btnCreateCategory");
        Label errorLabel = (Label) lookup("#lblError").query();
        Assert.assertEquals(errorLabel.getText(), "Name has to be at least 1 symbols long");

        TextField textField = (TextField) lookup("#tfCategoryName").query();
        textField.setText("testCategory");
        clickOn("#btnCreateCategory");

        JsonObject json = buildCreateCategoryFailure();
        mockCreateCategoryRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(errorLabel.getText(), "Something went wrong while creating the category");
    }

    @Test
    public void createChannelTest() {
<<<<<<< HEAD
        Category category = new Category().setId("12345");
        category.setServer(server);

        Platform.runLater(() -> {
            StageManager.showCreateChannelScreen(category);
=======
        Platform.runLater(() -> {
            StageManager.showCreateChannelScreen(new Category());
>>>>>>> TD21-58
        });
        WaitForAsyncUtils.waitForFxEvents();

        Button button = (Button) lookup("#btnCreateChannel").query();
        Assert.assertEquals(button.getText(), "Create");

        TextField textField = (TextField) lookup("#tfChannelName").query();
        textField.setText("testChannel");
        clickOn("#btnCreateChannel");

<<<<<<< HEAD
        JsonArray members = Json.createArrayBuilder().build();
        JsonObject json = buildCreateChannel(category.getId(), "4321", "testChannel", "text", false, members);
        mockCreateChannelRest(json);

        WaitForAsyncUtils.waitForFxEvents();

        Channel newChannel = null;
        for (Channel channel : category.getChannels()) {
            if (channel.getId().equals("4321")) {
                newChannel = channel;
            }
        }
        Assert.assertNotNull(newChannel);
        Assert.assertEquals(newChannel.getName(), "testChannel");
    }

    @Test
    public void createPrivilegedChannelTest() {
        JsonObject restJson = getServerIdSuccessful();
        mockRest(restJson);

        Category category = new Category().setId("12345");
        category.setServer(server);

        Platform.runLater(() -> {
            StageManager.showCreateChannelScreen(category);
        });
        WaitForAsyncUtils.waitForFxEvents();

        Button button = (Button) lookup("#btnCreateChannel").query();
        Assert.assertEquals(button.getText(), "Create");

        TextField textField = (TextField) lookup("#tfChannelName").query();
        textField.setText("testChannel");
        clickOn("#btnCreateChannel");

        JsonArray members = Json.createArrayBuilder().add(server.getMembers().get(0).getId()).build();
        JsonObject json = buildCreateChannel(category.getId(), "4321", "testChannel", "text", true, members);
        mockCreateChannelRest(json);


=======
        Category category = new Category();
        category.setId("12345");
        JsonObject json = buildCreateChannel("12345", "4321", "testChannel", "text");
        mockCreateChannelRest(json);
>>>>>>> TD21-58
        WaitForAsyncUtils.waitForFxEvents();

        Channel newChannel = null;
        for (Channel channel : category.getChannels()) {
            if (channel.getId().equals("4321")) {
                newChannel = channel;
            }
        }
        Assert.assertNotNull(newChannel);
        Assert.assertEquals(newChannel.getName(), "testChannel");
<<<<<<< HEAD
        Assert.assertEquals(newChannel.getMembers().get(0).getId(), server.getMembers().get(0).getId());
=======
>>>>>>> TD21-58
    }

    @Test
    public void createChannelFailureTest() {
<<<<<<< HEAD
        Category category = new Category().setId("12345");
        Platform.runLater(() -> {
            StageManager.showCreateChannelScreen(category);
=======
        Platform.runLater(() -> {
            StageManager.showCreateChannelScreen(new Category());
>>>>>>> TD21-58
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = (Button) lookup("#btnCreateChannel").query();
        Assert.assertEquals(button.getText(), "Create");

        clickOn("#btnCreateChannel");
        Label errorLabel = (Label) lookup("#lblError").query();
        Assert.assertEquals(errorLabel.getText(), "Name has to be at least 1 symbols long");

        TextField textField = (TextField) lookup("#tfChannelName").query();
        textField.setText("testChannel");
        clickOn("#btnCreateChannel");

        JsonObject json = buildCreateChannelFailure();
        mockCreateChannelRest(json);
        WaitForAsyncUtils.waitForFxEvents();

<<<<<<< HEAD
        Assert.assertEquals(errorLabel.getText(), "Something went wrong while creating the channel");
=======
        Assert.assertEquals(errorLabel.getText(), "Something went wrong while creating the category");
>>>>>>> TD21-58
    }

    @Test
    public void editChannelScreenControlerTest() {
        Platform.runLater(() -> {
            StageManager.showEditChannelScreen(new Channel());
            Button button = (Button) lookup("#btnEditChannel").query();
            Assert.assertEquals(button.getText(), "Save");
        });
    }

    // Methods for callbacks

    /**
     * create response when a category created
     */
    public JsonObject buildCreateCategory(String categoryId, String categoryName, String serverId) {
        return Json.createObjectBuilder().add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", categoryId)
                        .add("name", categoryName)
                        .add("server", serverId)
                ).build();
    }

    /**
     * create response when a category can not be created
     */
    public JsonObject buildCreateCategoryFailure() {
        return Json.createObjectBuilder().add("status", "failure")
                .add("message", "")
                .add("data", Json.createObjectBuilder()).build();
    }

    /**
     * create response when a channel is created
     */
<<<<<<< HEAD
    public JsonObject buildCreateChannel(String categoryId, String id, String channelName, String type, boolean privileged, JsonArray members) {
=======
    public JsonObject buildCreateChannel(String categoryId, String id, String channelName, String type) {
>>>>>>> TD21-58
        return Json.createObjectBuilder().add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", id)
                        .add("name", channelName)
                        .add("type", type)
<<<<<<< HEAD
                        .add("privileged", privileged)
                        .add("category", categoryId)
                        .add("members", members)
=======
                        .add("privileged", false)
                        .add("category", categoryId)
>>>>>>> TD21-58
                ).build();
    }

    /**
     * create response when a privileged channel is created
     */
    public JsonObject buildCreateChannelPrivileged(String categoryId, String id, String channelName, String type, boolean privileged, JsonArray members) {
        return Json.createObjectBuilder().add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", id)
                        .add("name", channelName)
                        .add("type", type)
                        .add("privileged", privileged)
                        .add("category", categoryId)
                        .add("members", members)
                ).build();
    }

    /**
     * create response when a channel can not be created
     */
    public JsonObject buildCreateChannelFailure() {
        return Json.createObjectBuilder().add("status", "failure")
                .add("message", "")
                .add("data", Json.createObjectBuilder()).build();
    }

    // websocket callbacks

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has joined
     */
    public JsonObject webSocketCallbackUserJoined() {
        return Json.createObjectBuilder().add("action", "userJoined").add("data",
                Json.createObjectBuilder().add("id", "123456").add("name", "Phil")).build();
    }

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has left
     */
    public JsonObject webSocketCallbackUserLeft() {
        return Json.createObjectBuilder().add("action", "userLeft").add("data",
                Json.createObjectBuilder().add("id", "123456").add("name", "Phil")).build();
    }

    public JsonObject webSocketCallbackServerUpdated() {
        return Json.createObjectBuilder().add("action", "serverUpdated").add("data",
                Json.createObjectBuilder().add("id", "testId").add("name", "serverUpdated")).build();
    }

    public JsonObject webSocketCallbackServerDeleted() {
        return Json.createObjectBuilder().add("action", "serverDeleted").add("data",
                Json.createObjectBuilder().add("id", "testId").add("name", "serverUpdated")).build();
    }

    public JsonObject webSocketCallbackUserExited() {
        return Json.createObjectBuilder().add("action", "userExited").add("data",
                Json.createObjectBuilder().add("id", "123456").add("name", "Phil")).build();
    }

    public JsonObject webSocketCallbackUserArrived() {
        return Json.createObjectBuilder().add("action", "userArrived").add("data",
                Json.createObjectBuilder().add("id", "12345678").add("name", "Tom").add("online", true)).build();
    }

    public JsonObject webSocketCallbackChannelCreated() {
        return Json.createObjectBuilder().add("action", "channelCreated").add("data",
                Json.createObjectBuilder().add("id", "ch1").add("name", "TestChannel").
                        add("type", "text").add("privileged", false).add("category", "cat1").add("members", Json.createArrayBuilder())).build();
    }

    public JsonObject webSocketCallbackChannelUpdated() {
        return Json.createObjectBuilder().add("action", "channelUpdated").add("data",
                Json.createObjectBuilder().add("id", "ch1").add("name", "channelUpdated").
                        add("type", "text").add("privileged", false).add("category", "cat1")).build();
    }

    public JsonObject webSocketCallbackChannelDeleted() {
        return Json.createObjectBuilder().add("action", "channelDeleted").add("data",
                Json.createObjectBuilder().add("id", "ch1").add("name", "channelUpdated")
                        .add("category", "cat1")).build();
    }

    public JsonObject webSocketCallbackCategoryCreated() {
        return Json.createObjectBuilder().add("action", "categoryCreated").add("data",
                Json.createObjectBuilder().add("id", "cat1").add("name", "Cat1").add("server", "testId")).build();
    }

    public JsonObject webSocketCallbackCategoryUpdated() {
        return Json.createObjectBuilder().add("action", "categoryUpdated").add("data",
                Json.createObjectBuilder().add("id", "cat1").add("name", "CatUpdated").add("server", "testId")).build();
    }

    public JsonObject webSocketCallbackCategoryDeleted() {
        return Json.createObjectBuilder().add("action", "categoryDeleted").add("data",
                Json.createObjectBuilder().add("id", "cat1").add("name", "CatUpdated")).build();
    }


    // rest callbacks

    public JsonObject getServerIdSuccessful() {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder().add("id", server.getId())
                        .add("name", server.getName()).add("owner", "ow12ner").add("categories",
                                Json.createArrayBuilder()).add("members", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("id", "I1").add("name", "N1")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "I2").add("name", "N2")
                                        .add("online", false))
                                .add(Json.createObjectBuilder().add("id", "I3").add("name", "N3")
                                        .add("online", true)))).build();
    }

    public JsonObject getServerIdFailure() {
        return Json.createObjectBuilder().add("status", "failure").add("message", "")
                .add("data", Json.createObjectBuilder()).build();
    }

    public JsonObject logoutSuccessful() {
        return Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "Logged out")
                .add("data", "{}")
                .build();
    }

    public JsonObject logoutFailure() {
        return Json.createObjectBuilder()
                .add("status", "failure")
                .add("message", "Log in first")
                .add("data", "{}")
                .build();
    }

    public JsonObject getCategories() {
        return Json.createObjectBuilder()
                .add("status", "success").add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df505")
                                .add("name", "Category1")
                                .add("server", "123").add("channels", Json.createArrayBuilder())
                        )
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df506")
                                .add("name", "Category2")
                                .add("server", "123").add("channels", Json.createArrayBuilder())
                        )
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df507")
                                .add("name", "Category3")
                                .add("server", "123").add("channels", Json.createArrayBuilder())
                        )).build();
    }

    public JsonObject getChannels() {
        return Json.createObjectBuilder()
                .add("status", "success").add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df505")
                                .add("name", "Channel_1")
                                .add("type", "text")
                                .add("privileged", false)
                                .add("category", "123").add("members", Json.createArrayBuilder())
                        )
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df506")
                                .add("name", "Channel_2")
                                .add("type", "text")
                                .add("privileged", false)
                                .add("category", "123").add("members", Json.createArrayBuilder())
                        )
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df507")
                                .add("name", "Channel_3")
                                .add("type", "text")
                                .add("privileged", false)
                                .add("category", "123").add("members", Json.createArrayBuilder())
                        )).build();
    }

    public JsonObject getTestMessageServerAnswer(JsonObject test_message) {
        return Json.createObjectBuilder()
                .add("id", "5e2ffbd8770dd077d03dr458")
                .add("channel", "idTest1")
                .add("timestamp", 1616935874)
                .add("from", localUser.getName())
                .add("text", test_message.getString(MESSAGE))
                .build();
    }

    public JsonObject getCategoryChannels() {
        return Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "idTest1")
                                .add("name", "channelName1")
                                .add("type", "text")
                                .add("privileged", false)
                                .add("category", "categoryId1")
                                .add("members", Json.createArrayBuilder()))
                        .add(Json.createObjectBuilder()
                                .add("id", "idTest2")
                                .add("name", "channelName2")
                                .add("type", "text")
                                .add("privileged", false)
                                .add("category", "categoryId2")
                                .add("members", Json.createArrayBuilder()))).build();
    }

    public JsonObject getServerCategories() {
        return Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "idTest")
                                .add("name", "categoryName")
                                .add("server", this.server.getId())
                                .add("channels", Json.createArrayBuilder().add("idTest")))).build();
    }

    public JsonObject getCategoryChannelsFailure() {
        return Json.createObjectBuilder()
                .add("status", "failure")
                .add("message", "")
                .add("data", Json.createArrayBuilder()).build();
    }

    public JsonObject getServerCategoriesFailure() {
        return Json.createObjectBuilder()
                .add("status", "failure")
                .add("message", "")
                .add("data", Json.createArrayBuilder()).build();
    }
}
