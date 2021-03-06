package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.ServerChatController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.richtext.RichTextArea;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.EmojiButton;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
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

import javax.json.*;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.SERVER_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * tests for the ServerScreenController
 * - user list view test
 * - logout test
 * - channel tree view test
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerScreenTest extends ApplicationTest {
    /**/
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    WebSocketClient webSocketClient;
    @Mock
    WebSocketClient chatWebSocketClient;
    @Mock
    WebSocketClient privateChatWebSocketClient;
    @Mock
    WebSocketClient systemWebSocketClient;

    private Stage stage;
    private Stage emojiPickerStage;
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
    private ArgumentCaptor<Callback<JsonNode>> joinServerCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> privateChatCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentCaptorWebSocket;

    private WSCallback wsCallback;

    @Captor
    private ArgumentCaptor<WSCallback> chatCallbackArgumentCaptorWebSocket;

    private Options oldOptions;

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
        this.emojiPickerStage = this.stageManager.getStage(StageEnum.EMOJI_PICKER_STAGE);
        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = this.stageManager.getEditor().haveLocalUser("JohnDoe", "testKey123");
        this.localUser.setPassword("secret").setId("123");
        this.server = this.stageManager.getEditor().haveServer(localUser, "testId", "TServer");
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(CHAT_USER_URL + this.stageManager.getEditor().
                getWebSocketManager().getCleanLocalUserName() + AND_SERVER_ID_URL + this.server.getId(), chatWebSocketClient);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);
        this.stageManager.initView(ControllerEnum.SERVER_SCREEN, server, null);

        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() {
        stageManager.getResourceManager().saveOptions(this.oldOptions);
        oldOptions = null;
        rule = null;
        webSocketClient = null;
        chatWebSocketClient = null;
        stage = null;
        emojiPickerStage = null;
        stageManager.stop();
        stageManager = null;
        localUser = null;
        server = null;
        restMock = null;
        res = null;
        callbackArgumentCaptor = null;
        channelsCallbackArgumentCaptor = null;
        channelCallbackArgumentCaptor = null;
        categoriesCallbackArgumentCaptor = null;
        joinServerCallbackArgumentCaptor = null;
        callbackArgumentCaptorWebSocket = null;
        wsCallback = null;
        chatCallbackArgumentCaptorWebSocket = null;
        privateChatWebSocketClient = null;
        systemWebSocketClient = null;
        privateChatCallbackArgumentCaptor = null;
    }

    public void mockRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock, atLeastOnce()).getExplicitServerInformation(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockPutUpdateMessageRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).updateMessage(anyString(), anyString(), any(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockDeleteMessageRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).deleteMessage(anyString(), any(), callbackArgumentCaptor.capture());

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

        verify(restMock, atLeastOnce()).getChannels(anyString(), anyString(), anyString(), channelCallbackArgumentCaptor.capture());

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

    public void mockJoinServerRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));
        when(res.isSuccess()).thenReturn(true);

        verify(restMock).joinServer(any(), anyString(), joinServerCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = joinServerCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockChatWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(chatWebSocketClient, atLeastOnce()).setCallback(chatCallbackArgumentCaptorWebSocket.capture());
        WSCallback chatWsCallback = chatCallbackArgumentCaptorWebSocket.getValue();

        chatWsCallback.handleMessage(webSocketJson);
    }

    public void mockPrivateChatWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(privateChatWebSocketClient).setCallback(privateChatCallbackArgumentCaptor.capture());
        WSCallback wsSystemCallback = privateChatCallbackArgumentCaptor.getValue();

        wsSystemCallback.handleMessage(webSocketJson);
    }


    @Test
    public void initUserListView() {
        JsonObject restJson = getServerIdSuccessful();
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        ListView<Object> listView = lookup("#lvServerUsers").queryListView();
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertEquals(0, listView.getItems().toArray().length);
        mockRest(restJson);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(4, listView.getItems().toArray().length);
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(0)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(1)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(2)));
        Assert.assertFalse(listView.getItems().contains(new Server()));

        mockWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void initChannelListView() {
        JsonObject categoriesRestJson = getServerCategories();
        mockGetCategoryRest(categoriesRestJson);
        WaitForAsyncUtils.waitForFxEvents();
        JsonObject channelRestJson = getCategoryChannels();
        mockChannelRest(channelRestJson);
    }

    public void initChannelListViewChannelFailure() {
        JsonObject categoriesRestJson = getServerCategories();
        mockGetCategoryRest(categoriesRestJson);
        WaitForAsyncUtils.waitForFxEvents();
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
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(4, listView.getItems().toArray().length);
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

        // first have to open optionScreen
        clickOn("#btnOptions");
        Assert.assertEquals("Options - Appearance", stage.getTitle());

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

        // first have to open optionScreen
        clickOn("#btnOptions");
        Assert.assertEquals("Options - Appearance", stage.getTitle());

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
        Assert.assertEquals("Private Chats", stage.getTitle());
    }


    @Test
    public void initChannelsTest() {
        JsonObject restJson = getServerIdSuccessful();
        ListView<Object> listView = lookup("#lvServerUsers").queryListView();
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertEquals(0, listView.getItems().toArray().length);
        mockRest(restJson);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(4, listView.getItems().toArray().length);
        Assert.assertEquals(server.getMembers().toArray().length, listView.getItems().toArray().length);
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(0)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(1)));
        Assert.assertTrue(listView.getItems().contains(server.getMembers().get(2)));
        Assert.assertFalse(listView.getItems().contains(new Server()));

        when(res.getBody()).thenReturn(new JsonNode(getCategories().toString()));
        verify(restMock).getCategories(anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> catCallback = callbackArgumentCaptor.getValue();
        catCallback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();
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
        Assert.assertEquals("Category1", categoryOne.getName());

        Assert.assertEquals(3, categoryOne.getChannels().size());

        // click on one channel and check if messages loaded correctly
        clickOn("Channel_3");
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        when(res.getBody()).thenReturn(new JsonNode(getChannelMessage(channel).toString()));
        verify(restMock).getChannelMessages(anyString(), anyString(), anyString(), anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> channelMessageCallback = callbackArgumentCaptor.getValue();
        channelMessageCallback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        ObservableList<Message> items = lvTextChat.getItems();
        Assert.assertEquals(items.size(), 2);
        Assert.assertEquals(items.get(0).getText(), "Hello there!");
        Assert.assertEquals(items.get(1).getText(), "I am Bob");
    }

    @Test
    public void getChannelMessageFailure() {
        JsonObject restJson = getServerIdSuccessful();
        mockRest(restJson);

        when(res.getBody()).thenReturn(new JsonNode(getCategories().toString()));
        verify(restMock).getCategories(anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> catCallback = callbackArgumentCaptor.getValue();
        catCallback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(getChannels().toString()));

        verify(restMock, atLeastOnce()).getChannels(anyString(), anyString(), anyString(), channelsCallbackArgumentCaptor.capture());
        List<Callback<JsonNode>> channelCallbacks = channelsCallbackArgumentCaptor.getAllValues();

        for (Callback<JsonNode> callback : channelCallbacks
        ) {
            callback.completed(res);
        }

        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Channel_3");

        when(res.getBody()).thenReturn(new JsonNode(getChannelMessagesFailure().toString()));
        verify(restMock).getChannelMessages(anyString(), anyString(), anyString(), anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> channelMessageCallback = callbackArgumentCaptor.getValue();
        channelMessageCallback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(stage.getTitle(), "Private Chats");
    }

    @Test
    public void loadMoreMessagesTest() {
        JsonObject restJson = getServerIdSuccessful();
        mockRest(restJson);

        when(res.getBody()).thenReturn(new JsonNode(getCategories().toString()));
        verify(restMock).getCategories(anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> catCallback = callbackArgumentCaptor.getValue();
        catCallback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(getChannels().toString()));

        verify(restMock, atLeastOnce()).getChannels(anyString(), anyString(), anyString(), channelsCallbackArgumentCaptor.capture());
        List<Callback<JsonNode>> channelCallbacks = channelsCallbackArgumentCaptor.getAllValues();

        for (Callback<JsonNode> callback : channelCallbacks
        ) {
            callback.completed(res);
        }

        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Channel_3");
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        when(res.getBody()).thenReturn(new JsonNode(build50Messages(channel).toString()));
        verify(restMock).getChannelMessages(anyString(), anyString(), anyString(), anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> channelMessageCallback = callbackArgumentCaptor.getValue();
        channelMessageCallback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        ObservableList<Message> items = lvTextChat.getItems();
        Assert.assertEquals(items.size(), 51);
        Assert.assertEquals(items.get(0).getText(), "Load more...");

        lvTextChat.scrollTo(0);

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("Load more...");

        when(res.getBody()).thenReturn(new JsonNode(getChannelMessage(channel).toString()));
        verify(restMock, atLeastOnce()).getChannelMessages(anyString(), anyString(), anyString(), anyString(), anyString(), callbackArgumentCaptor.capture());
        channelMessageCallback = callbackArgumentCaptor.getValue();
        channelMessageCallback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        items = lvTextChat.getItems();
        Assert.assertEquals(items.get(0).getText(), "Hello there!");
        Assert.assertEquals(items.get(1).getText(), "I am Bob");
    }

    @Test
    public void sendChatMessageTest() {
        //init channel list and select first channel
        initUserListView();
        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();


        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());

        Button emojiButton = lookup("#btnEmoji").queryButton();
        clickOn(emojiButton);

        WaitForAsyncUtils.waitForFxEvents();
        //Assert.assertTrue(emojiPickerStage.isShowing());
        Assert.assertEquals("Emoji Picker", emojiPickerStage.getTitle());

        GridPane panelForEmojis = (GridPane) emojiPickerStage.getScene().getRoot().lookup("#panelForEmojis");
        EmojiButton emoji = (EmojiButton) panelForEmojis.getChildren().get(0);
        clickOn(emoji);

        //send message
        ((RichTextArea) lookup("#tfInputMessage").query()).setText(((RichTextArea) lookup("#tfInputMessage").query()).getText() + "Test Message");
        clickOn("#tfInputMessage");

        press(KeyCode.ENTER);

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "Test Message" + emoji.getText());
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lvTextChat.getItems().size());
        Assert.assertEquals(channel.getMessages().size(), lvTextChat.getItems().size());
        Assert.assertEquals(lvTextChat.getItems().get(0), channel.getMessages().get(0));
        Assert.assertEquals(lvTextChat.getItems().get(0).getText(), channel.getMessages().get(0).getText());
        Assert.assertEquals("Test Message" + emoji.getText(), lvTextChat.getItems().get(0).getText());

        ((RichTextArea) lookup("#tfInputMessage").query()).setText("%Spoiler Message%");
        clickOn("#tfInputMessage");
        press(KeyCode.ENTER);

        JsonObject testSpoilerMessage = JsonUtil.buildServerChatMessage(channel.getId(), "%Spoiler Message%");
        mockChatWebSocket(getTestMessageServerAnswer(testSpoilerMessage));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Spoiler");
        Assert.assertEquals(channel.getMessages().size(), lvTextChat.getItems().size());
        Assert.assertEquals(lvTextChat.getItems().get(0), channel.getMessages().get(1));
        Assert.assertEquals(lvTextChat.getItems().get(0).getText(), channel.getMessages().get(1).getText());
        Assert.assertEquals("Spoiler Message", lvTextChat.getItems().get(0).getText());

    }

    @Test
    public void markingTest() {
        initUserListView();
        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();
        RichTextArea tfInputMessage = lookup("#tfInputMessage").query();

        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());

        tfInputMessage.setText("");
        clickOn("#tfInputMessage");

        write("@N");
        WaitForAsyncUtils.waitForFxEvents();
        ListView<User> selectUser = lookup("#lvSelectUser").queryListView();

        Assert.assertEquals(selectUser.getItems().size(), 3);

        tfInputMessage.moveTo(1);
        press(KeyCode.DELETE);
        write('\b');
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(selectUser.getItems().size(), 4);

        clickOn("#@N2");
        WaitForAsyncUtils.waitForFxEvents();

        String text = tfInputMessage.getText();
        Assert.assertEquals("@N2", text);

        clickOn("#tfInputMessage");
        write("@N1");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertFalse(selectUser.isVisible());

        Assert.assertEquals(tfInputMessage.getText(), "@N2@N1");
        TextArea s = new TextArea();
        s.positionCaret(2);
        tfInputMessage.moveTo(2);

        write("1");
        Assert.assertEquals(tfInputMessage.getText(), "@N1");
        tfInputMessage.moveTo(3);

        press(KeyCode.BACK_SPACE);
        write('\b');
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(tfInputMessage.getText().isEmpty());

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "@JohnDoe");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#tfInputMessage");
        write("@");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(selectUser.isVisible());
        write("3");
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.BACK_SPACE);
        push(KeyCode.BACK_SPACE);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(selectUser.isVisible());
    }

    @Test
    public void referenceTest() {
        initUserListView();
        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();
        RichTextArea tfInputMessage = lookup("#tfInputMessage").query();

        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());
        Assert.assertEquals(channel.getName(), "channelName1");

        tfInputMessage.setText("");
        clickOn("#tfInputMessage");

        write("#channel");
        WaitForAsyncUtils.waitForFxEvents();
        ListView<Channel> selectChannel = lookup("#lvSelectChannel").queryListView();

        Assert.assertEquals(selectChannel.getItems().size(), 2);

        selectChannel.getSelectionModel().select(0);
        clickOn("#lvSelectChannel");

        Assert.assertEquals("#channelName2", tfInputMessage.getText());

        clickOn("#tfInputMessage");
        write("#channelName2");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertFalse(selectChannel.isVisible());

        Assert.assertEquals(tfInputMessage.getText(), "#channelName2#channelName2");
        TextArea s = new TextArea();
        s.positionCaret(2);
        tfInputMessage.moveTo(2);

        write("1");
        Assert.assertEquals(tfInputMessage.getText(), "#channelName2");
        tfInputMessage.moveTo(3);

        press(KeyCode.BACK_SPACE);
        write('\b');
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(tfInputMessage.getText().isEmpty());

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "@JohnDoe");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#tfInputMessage");
        write("#");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(selectChannel.isVisible());
        write("3");
        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.BACK_SPACE);

        JsonObject webSocketJson = getChannelMessageReference(channel);
        mockChatWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();

        ObservableList<Message> items = lvTextChat.getItems();
        Assert.assertEquals(items.size(), 2);

        Assert.assertEquals("#channelName2 1#channelName2 1", items.get(0).getText());

        clickOn("#reference");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("channelName2", lblChannelName.getText());

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(tvServerChannels.getSelectionModel().getSelectedItem().getValue() instanceof Channel);
        Assert.assertEquals("channelName2", ((Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue()).getName());

        clickOn("#tfInputMessage");
        write("@#@");
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
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Login", stage.getTitle());
    }

    @Test
    public void testChatMessagesCachedProperlyAfterChannelChange() {
        //init user list and select first user
        initUserListView();
        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();
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
        ((RichTextArea) lookup("#tfInputMessage").query()).setText("Test Message");
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
        Assert.assertTrue(channel.isRead());

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
        Assert.assertTrue(channel.isRead());
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
        Assert.assertEquals("Private Chats", stage.getTitle());

        channel = server.getCategories().get(0).getChannels().get(0);
        new Message().setText("Test Message").setChannel(channel).setId("5e2ffbd8770dd077d03dr458");
        Message message = channel.getMessages().get(0);
        Assert.assertEquals(message.getText(), "Test Message");
        mockWebSocket(webSocketCallbackMessageUpdated("New Message"));
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(message.getText(), "New Message");

        mockWebSocket(webSocketCallbackMessageUpdatedError());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Private Chats", stage.getTitle());

        mockWebSocket(webSocketCallbackMessageDeleted());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getMessages().size(), 0);

        mockWebSocket(webSocketCallbackMessageDeleteError());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Private Chats", stage.getTitle());

        phil.withServers(server);
        Assert.assertEquals(channel.getAudioMembers().size(), 0);
        mockWebSocket(webSocketCallbackAudioJoined());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getAudioMembers().size(), 1);
        Assert.assertEquals(channel.getAudioMembers().get(0), phil);

        mockWebSocket(webSocketCallbackAudioLeft());
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getAudioMembers().size(), 0);
    }


    @Test
    public void leaveServerTest() {
        openAttentionScreen();
        testBtnCancel();
        openAttentionScreen();
        testBtnLeave();
    }

    private void testBtnLeave() {

        JsonObject json = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "Successfully exited")
                .add("data", "{}")
                .build();
        when(res.getBody()).thenReturn(new JsonNode(json.toString()));


        Assert.assertEquals("success", res.getBody().getObject().getString("status"));

        WaitForAsyncUtils.waitForFxEvents();
        Button btnLeave = lookup("#btnLeave").query();
        Assert.assertEquals(btnLeave.getText(), "Leave");

        clickOn(btnLeave);

        verify(restMock).leaveServer(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callbackLeaveServer = callbackArgumentCaptor.getValue();
        callbackLeaveServer.completed(res);

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(this.stageManager.getStage(StageEnum.STAGE).getTitle(), "Private Chats");

    }

    private void testBtnCancel() {
        WaitForAsyncUtils.waitForFxEvents();
        Button btnCancel = lookup("#btnCancel").query();
        Assert.assertEquals(btnCancel.getText(), "Cancel");
        clickOn(btnCancel);
        Assert.assertEquals(this.stageManager.getStage(StageEnum.STAGE).getTitle(), "Server");

    }

    private void openAttentionScreen() {
        Platform.runLater(() -> this.stageManager.initView(ControllerEnum.ATTENTION_LEAVE_SERVER_SCREEN, server, null));
    }

    @Test
    public void testQuote() {
        //init channel list and select first channel

        initUserListView();
        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();
        RichTextArea tfInputMessage = lookup("#tfInputMessage").query();

        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());

        //send message
        clickOn("#tfInputMessage");
        ((RichTextArea) lookup("#tfInputMessage").query()).setText("Test Message");
        press(KeyCode.ENTER);

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message, 1616935874));
        WaitForAsyncUtils.waitForFxEvents();

        lvTextChat.getSelectionModel().select(0);
        rightClickOn(lvTextChat);

        clickOn("- quote");
        WaitForAsyncUtils.waitForFxEvents();

        Button btnCancelQuote = lookup("#btnCancelQuote").query();

        ServerScreenController serverScreenController = (ServerScreenController) stageManager.getControllerMap().get(SERVER_SCREEN_CONTROLLER);
        ServerChatController controller = serverScreenController.getServerChatController();

        String formatted = this.stageManager.getEditor().getMessageManager().getMessageFormatted(lvTextChat.getItems().get(0), lvTextChat.getItems().get(0).getText());
        Assert.assertEquals(controller.getQuotedText(), formatted);
        clickOn(btnCancelQuote);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(controller.getQuotedText(), "");


        lvTextChat.getSelectionModel().select(0);
        rightClickOn(lvTextChat);
        clickOn("- quote");
        WaitForAsyncUtils.waitForFxEvents();

        formatted = this.stageManager.getEditor().getMessageManager().getMessageFormatted(lvTextChat.getItems().get(0), lvTextChat.getItems().get(0).getText());
        Assert.assertEquals(controller.getQuotedText(), formatted);

        ((RichTextArea) lookup("#tfInputMessage").query()).setText("quote");
        clickOn("#tfInputMessage");
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        JsonObject quote = JsonUtil.buildServerChatMessage(channel.getId(), QUOTE_PREFIX + formatted + QUOTE_MESSAGE + "123" + QUOTE_SUFFIX);
        JsonObject quote_message = JsonUtil.buildServerChatMessage(channel.getId(), "quote");
        mockChatWebSocket(getTestMessageServerAnswer(quote, 1616935875));
        mockChatWebSocket(getTestMessageServerAnswer(quote_message, 1616935876));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(lvTextChat.getItems().get(1).getText(), QUOTE_PREFIX + formatted + QUOTE_MESSAGE + "123" + QUOTE_SUFFIX);
        Assert.assertEquals(lvTextChat.getItems().get(2).getText(), "quote");
    }


    @Test
    public void descriptionTest() {
        //init channel list and select first channel

        JsonObject restJson = getServerIdSuccessfulWithDescriptions();
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        ListView<Object> lvServerUsers = lookup("#lvServerUsers").queryListView();
        mockRest(restJson);
        WaitForAsyncUtils.waitForFxEvents();
        mockWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();

        initChannelListView();

        WaitForAsyncUtils.waitForFxEvents();

        User n1 = null;
        User n2 = null;
        for (User user : server.getMembers()) {
            if (user.getName().equals("N1")) {
                n1 = user;
            }
            if (user.getName().equals("N2")) {
                n2 = user;
            }
        }
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertTrue(n1.getDescription().equals("+plays a game"));
        Assert.assertTrue(n2.getDescription().equals("+is afk"));

        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(PRIVATE_USER_CHAT_PREFIX +
                this.stageManager.getEditor().getWebSocketManager().getCleanLocalUserName(), privateChatWebSocketClient);

        this.stageManager.getEditor().getWebSocketManager().start();

        verify(systemWebSocketClient).setCallback(callbackArgumentCaptorWebSocket.capture());
        this.wsCallback = callbackArgumentCaptorWebSocket.getValue();

        this.wsCallback.handleMessage(changeDescription());
        WaitForAsyncUtils.waitForFxEvents();

        for (User user : server.getMembers()) {
            if (user.getId().equals(n1.getId())) {
                n1 = user;
            }
        }
        Assert.assertEquals("+newTest", n1.getDescription());

    }

    @Test
    public void testReferencedMessage() {
        //init channel list and select first channel
        initUserListView();
        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();


        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());

        //send message
        ((RichTextArea) lookup("#tfInputMessage").query()).setText(((RichTextArea) lookup("#tfInputMessage").query()).getText() + "Test Message");
        clickOn("#tfInputMessage");

        press(KeyCode.ENTER);

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, lvTextChat.getItems().size());
        Assert.assertEquals(channel.getMessages().size(), lvTextChat.getItems().size());
        Assert.assertEquals(lvTextChat.getItems().get(0), channel.getMessages().get(0));
        Assert.assertEquals(lvTextChat.getItems().get(0).getText(), channel.getMessages().get(0).getText());

        lvTextChat.getSelectionModel().select(0);
        rightClickOn(lvTextChat);

        clickOn("- copy message link");
        test_message = JsonUtil.buildServerChatMessage(channel.getId(), "messageLink/testId/idTest/idTest1/5e2ffbd8770dd077d03dr458/1616935874");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));

        final Clipboard clipboard = Clipboard.getSystemClipboard();
        Platform.runLater(() -> {

            Assert.assertEquals(clipboard.getString(), "messageLink/testId/idTest/idTest1/5e2ffbd8770dd077d03dr458/1616935874");
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testReferencedMessageOpen() {
        //init channel list and select first channel
        Message message = new Message().setText("Test").setTimestamp(1616935874).setId("5e2ffbd8770dd077d03dr458").setFrom("Tom");
        server.withCategories(new Category().setId("idTest").withChannels(new Channel().setId("idTest1")
                .withMessages(message)));
        server.setReferenceMessage("messageLink/testId/idTest/idTest1/5e2ffbd8770dd077d03dr458/1616935874");
        initUserListView();
        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(Json.createObjectBuilder().add("status", "success")
                .add("data", Json.createArrayBuilder()).build().toString()));

        verify(restMock).getChannelMessages(anyString(), anyString(), anyString(), anyString(), anyString(), categoriesCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = categoriesCallbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        Assert.assertEquals(lvTextChat.getSelectionModel().getSelectedItem(), message);
    }

    @Test
    public void testUpdateMessage() {
        //init channel list and select first channel

        initUserListView();
        initChannelListView();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();

        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());

        //send message
        clickOn("#tfInputMessage");

        ((RichTextArea) lookup("#tfInputMessage").query()).setText("Test Message");
        press(KeyCode.ENTER);

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message, 1616935874));
        WaitForAsyncUtils.waitForFxEvents();

        lvTextChat.getSelectionModel().select(0);
        rightClickOn(lvTextChat);

        clickOn("- edit message");
        WaitForAsyncUtils.waitForFxEvents();

        RichTextArea tArea = lookup("#tfUpdateMessage").query();
        tArea.setText("update");
        Button btn = lookup("#btnEmojiUpdateMessage").queryButton();
        clickOn(btn);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(emojiPickerStage.isShowing());
        Assert.assertEquals("Emoji Picker", emojiPickerStage.getTitle());


        GridPane panelForEmojis = (GridPane) emojiPickerStage.getScene().getRoot().lookup("#panelForEmojis");
        EmojiButton emoji = (EmojiButton) panelForEmojis.getChildren().get(0);
        clickOn(emoji);


        clickOn("#btnUpdateMessage");
        WaitForAsyncUtils.waitForFxEvents();

        String message = ((RichTextArea) lookup("#tfUpdateMessage").query()).getText();
        mockWebSocket(webSocketCallbackMessageUpdated(message));
        WaitForAsyncUtils.waitForFxEvents();

        Message newMessage = lvTextChat.getItems().get(0);
        Assert.assertEquals(message, newMessage.getText());
    }

    @Test
    public void testDeleteMessage() {
        //init channel list and select first channel

        initUserListView();
        initChannelListView();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();

        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());

        //send message
        clickOn("#tfInputMessage");
        WaitForAsyncUtils.waitForFxEvents();

        ((RichTextArea) lookup("#tfInputMessage").query()).setText("Test Message");
        press(KeyCode.ENTER);

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message, 1616935874));
        WaitForAsyncUtils.waitForFxEvents();

        lvTextChat.getSelectionModel().select(0);
        rightClickOn(lvTextChat);
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("- delete message");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#btnDelete");
        WaitForAsyncUtils.waitForFxEvents();

        mockDeleteMessageRest(deleteMessageFailure());
        WaitForAsyncUtils.waitForFxEvents();

        Label lblError = lookup("#lblError").query();
        Assert.assertEquals("Error. Delete Message was not successful!", lblError.getText());

        clickOn("#btnDiscard");
        WaitForAsyncUtils.waitForFxEvents();

        lvTextChat.getSelectionModel().select(0);
        rightClickOn(lvTextChat);
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("- delete message");
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#btnDelete");
        WaitForAsyncUtils.waitForFxEvents();

        mockWebSocket(webSocketCallbackMessageDeleted());
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(0, lvTextChat.getItems().size());
    }

    @Test
    public void updateMessageFailureTest() {
        initUserListView();
        initChannelListView();
        Label lblChannelName = lookup("#lbChannelName").query();
        ListView<Message> lvTextChat = lookup("#lvTextChat").queryListView();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();

        WaitForAsyncUtils.waitForFxEvents();
        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());

        //send message
        clickOn("#tfInputMessage");

        ((RichTextArea) lookup("#tfInputMessage").query()).setText("Test Message");
        press(KeyCode.ENTER);

        JsonObject test_message = JsonUtil.buildServerChatMessage(channel.getId(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message, 1616935874));
        WaitForAsyncUtils.waitForFxEvents();

        lvTextChat.getSelectionModel().select(0);
        rightClickOn(lvTextChat);

        clickOn("- edit message");
        WaitForAsyncUtils.waitForFxEvents();

        ((RichTextArea) lookup("#tfUpdateMessage").query()).setText("update");
        clickOn("#btnUpdateMessage");
        WaitForAsyncUtils.waitForFxEvents();

        mockPutUpdateMessageRest(putUpdateMessageFailure());
        WaitForAsyncUtils.waitForFxEvents();

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("An error occurred, please try again later!", errorLabel.getText());

        clickOn("#btnDiscard");

        lvTextChat.getSelectionModel().select(0);
        rightClickOn(lvTextChat);

        clickOn("- edit message");
        WaitForAsyncUtils.waitForFxEvents();

        ((RichTextArea) lookup("#tfUpdateMessage").query()).setText("");
        clickOn("#btnUpdateMessage");
        WaitForAsyncUtils.waitForFxEvents();
        errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Updated message needs at least 1 character!", errorLabel.getText());
    }


    @Test
    public void privateMessageTest() {
        // some more Mocking that is required to send private Messages
        stageManager.getEditor().setUpDB();

        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(PRIVATE_USER_CHAT_PREFIX +
                this.stageManager.getEditor().getWebSocketManager().getCleanLocalUserName(), privateChatWebSocketClient);

        this.stageManager.getEditor().getWebSocketManager().start();


        JsonObject restJson = getServerIdSuccessful();
        ListView<Object> listView = lookup("#lvServerUsers").queryListView();
        mockRest(restJson);
        WaitForAsyncUtils.waitForFxEvents();

        // select certain user
        clickOn("Phil");
        User phil = (User) listView.getSelectionModel().getSelectedItem();
        phil.setPrivateChat(new Chat());

        Assert.assertEquals(phil.getPrivateChat().getMessages().size(), 0);

        Platform.runLater(() -> stageManager.initView(ControllerEnum.PRIVATE_MESSAGE_SERVER_SCREEN, server, phil));
        WaitForAsyncUtils.waitForFxEvents();
        //Assert.assertEquals(stageManager.getStage(StageEnum.POPUP_STAGE).getTitle(), phil.getName());

        RichTextArea tfMessage = lookup("#tfMessage").query();

        Assert.assertEquals(tfMessage.isEditable(), phil.isOnlineStatus());
        Assert.assertEquals(tfMessage.getPromptText(), phil.getName() + " " + LanguageResolver.getString("IS_OFFLINE"));

        phil.setOnlineStatus(true);

        Assert.assertEquals(tfMessage.isEditable(), phil.isOnlineStatus());
        Assert.assertEquals(tfMessage.getPromptText(), "Send Message to " + phil.getName());

        // Assert send message with emoji is working correctly
        tfMessage.setText("Hello Phil");
        WaitForAsyncUtils.waitForFxEvents();
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();

        JsonObject test_message = buildPrivateChatMessage(phil.getName(), "Hello Phil");
        mockPrivateChatWebSocket(test_message);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(phil.getPrivateChat().getMessages().size(), 1);

        // Assert changing to privateChat works correctly
        tfMessage.setText("How are you?");
        clickOn("#btnShowChat");

        RichTextArea tfEnterPrivateChat = lookup("#tfEnterPrivateChat").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();

        PrivateMessage message = lwPrivateChat.getItems().get(0);

        Assert.assertEquals(message.getText(), "Hello Phil");
        Assert.assertEquals(tfEnterPrivateChat.getText(), "How are you?");
    }

    @Test
    public void joinServerThroughMessage() {
        initUserListView();
        WaitForAsyncUtils.waitForFxEvents();
        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();
        Label lblChannelName = lookup("#lbChannelName").query();
        TreeView<Object> tvServerChannels = lookup("#tvServerChannels").query();

        tvServerChannels.getSelectionModel().select(1);
        Channel channel = (Channel) tvServerChannels.getSelectionModel().getSelectedItem().getValue();

        clickOn("#tvServerChannels");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(channel.getName(), lblChannelName.getText());


        Message message = new Message().setId("msgId123")
                .setText("https://ac.uniks.de/api/servers/5e2ffbd8770dd077d03df505/invites/5e2ffbd8770dd077d445qs900")
                .setFrom(localUser.getName())
                .setTimestamp(723978122);
        channel.withMessages(message);

        WaitForAsyncUtils.waitForFxEvents();

        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + "5e2ffbd8770dd077d03df505", webSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(CHAT_USER_URL + this.stageManager.getEditor().
                getWebSocketManager().getCleanLocalUserName() + AND_SERVER_ID_URL + "5e2ffbd8770dd077d03df505", chatWebSocketClient);

        Assert.assertEquals(localUser.getServers().size(), 1);

        clickOn(LanguageResolver.getString("JOIN"));

        mockJoinServerRest(joinServer());

        WaitForAsyncUtils.waitForFxEvents();

        mockRest(getNewServerSuccessful());

        WaitForAsyncUtils.waitForFxEvents();

        Label lbServerName = lookup("#lbServerName").query();
        Assert.assertEquals(lbServerName.getText(), "new Server");
        Assert.assertEquals(localUser.getServers().size(), 2);
    }

    @Test
    public void userServerMenuButtonTest() {
        JsonObject restJson = getServerIdSuccessful();
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        mockRest(restJson);
        WaitForAsyncUtils.waitForFxEvents();
        mockWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();

        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();
        MenuButton serverMenuButton = lookup("#serverMenuButton").query();
        Assert.assertEquals(2, serverMenuButton.getItems().size());
        Assert.assertEquals(LanguageResolver.getString("LEAVE_SERVER"), serverMenuButton.getItems().get(0).getText());
        Assert.assertEquals(LanguageResolver.getString("ADD_CATEGORY"), serverMenuButton.getItems().get(1).getText());
        serverMenuButton.getItems().get(0).setId("LEAVE_SERVER");
        serverMenuButton.getItems().get(1).setId("ADD_CATEGORY");
        clickOn(serverMenuButton).clickOn("#LEAVE_SERVER");

        Assert.assertEquals(stageManager.getStage(StageEnum.POPUP_STAGE).getTitle(), "Attention");

        clickOn("#btnCancel");

        clickOn(serverMenuButton).clickOn("#ADD_CATEGORY");

        Assert.assertEquals(stageManager.getStage(StageEnum.POPUP_STAGE).getTitle(), "Add Category");

    }

    @Test
    public void ownerServerMenuButtonTest() {
        JsonObject restJson = getServerIdSuccessfulAsOwner();
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        mockRest(restJson);
        WaitForAsyncUtils.waitForFxEvents();
        mockWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();

        initChannelListView();
        WaitForAsyncUtils.waitForFxEvents();
        MenuButton serverMenuButton = lookup("#serverMenuButton").query();
        Assert.assertEquals(2, serverMenuButton.getItems().size());
        Assert.assertEquals(LanguageResolver.getString("SERVER_SETTINGS"), serverMenuButton.getItems().get(0).getText());
        Assert.assertEquals(LanguageResolver.getString("ADD_CATEGORY"), serverMenuButton.getItems().get(1).getText());
        serverMenuButton.getItems().get(0).setId("SERVER_SETTINGS");
        serverMenuButton.getItems().get(1).setId("ADD_CATEGORY");
        clickOn(serverMenuButton).clickOn("#SERVER_SETTINGS");

        Assert.assertEquals(stageManager.getStage(StageEnum.POPUP_STAGE).getTitle(), "Edit Server");

        clickOn("#btnSave");

        clickOn(serverMenuButton).clickOn("#ADD_CATEGORY");

        Assert.assertEquals(stageManager.getStage(StageEnum.POPUP_STAGE).getTitle(), "Add Category");

    }

    // Methods for callbacks

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has joined
     */
    public JsonObject webSocketCallbackUserJoined() {
        return Json.createObjectBuilder().add("action", "userJoined").add("data",
                Json.createObjectBuilder().add("id", "123456").add("name", "Phil").add("description", "")).build();
    }

    private JsonStructure changeDescription() {
        return Json.createObjectBuilder().add("action", USER_DESCRIPTION_CHANGED).add("data",
                Json.createObjectBuilder().add("id", "I1").add("description", "+newTest")).build();

    }

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has left
     */
    public JsonObject webSocketCallbackUserLeft() {
        return Json.createObjectBuilder().add("action", "userLeft").add("data",
                Json.createObjectBuilder().add("id", "123456").add("name", "Phil").add("description", "")).build();
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
                Json.createObjectBuilder().add("id", "123456").add("description", "").add("name", "Phil")).build();
    }

    public JsonObject webSocketCallbackUserArrived() {
        return Json.createObjectBuilder().add("action", "userArrived").add("data",
                Json.createObjectBuilder().add("id", "12345678").add("name", "Tom").add("description", "").add("online", true)).build();
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

    public JsonObject webSocketCallbackMessageUpdated(String text) {
        return Json.createObjectBuilder().add("action", "messageUpdated").add("data",
                        Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03dr458")
                                .add("category", "idTest")
                                .add("channel", "idTest1")
                                .add("text", text))
                .build();
    }

    public JsonObject webSocketCallbackMessageDeleted() {
        return Json.createObjectBuilder().add("action", "messageDeleted").add("data",
                        Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03dr458")
                                .add("category", "idTest")
                                .add("channel", "idTest1"))
                .build();
    }

    public JsonObject webSocketCallbackMessageUpdatedError() {
        return Json.createObjectBuilder().add("action", "messageUpdated").add("data",
                        Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03dr458")
                                .add("category", "idTest")
                                .add("channel", "idInvalid")
                                .add("text", "error"))
                .build();
    }

    public JsonObject webSocketCallbackMessageDeleteError() {
        return Json.createObjectBuilder().add("action", "messageDeleted").add("data",
                        Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03dr458")
                                .add("category", "idTest")
                                .add("channel", "idInvalid")
                                .add("text", "error"))
                .build();
    }

    public JsonObject webSocketCallbackAudioJoined() {
        return Json.createObjectBuilder().add("action", "audioJoined").add("data",
                        Json.createObjectBuilder()
                                .add("category", "idTest")
                                .add("channel", "idTest1")
                                .add("id", "123456"))
                .build();
    }

    public JsonObject webSocketCallbackAudioLeft() {
        return Json.createObjectBuilder().add("action", "audioLeft").add("data",
                        Json.createObjectBuilder()
                                .add("category", "idTest")
                                .add("channel", "idTest1")
                                .add("id", "123456"))
                .build();
    }

    // rest callbacks

    public JsonObject getServerIdSuccessful() {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder().add("id", server.getId())
                        .add("name", server.getName()).add("owner", "ow12ner").add("categories",
                                Json.createArrayBuilder()).add("members", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("id", "I1").add("name", "N1").add("description", "")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "I2").add("name", "N2").add("description", "")
                                        .add("online", false))
                                .add(Json.createObjectBuilder().add("id", "I3").add("name", "N3").add("description", "")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "123456").add("name", "Phil").add("description", "")
                                        .add("online", false))
                        )).build();
    }

    public JsonObject getServerIdSuccessfulWithDescriptions() {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder().add("id", server.getId())
                        .add("name", server.getName()).add("owner", "ow12ner").add("categories",
                                Json.createArrayBuilder()).add("members", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("id", "I1").add("name", "N1").add("description", "+plays a game")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "I2").add("name", "N2").add("description", "+is afk")
                                        .add("online", false))
                                .add(Json.createObjectBuilder().add("id", "I3").add("name", "N3").add("description", "+")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "123456").add("name", "Phil").add("description", "+")
                                        .add("online", false))
                        )).build();
    }

    public JsonObject getServerIdSuccessfulAsOwner() {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder().add("id", server.getId())
                        .add("name", server.getName()).add("owner", "123").add("categories",
                                Json.createArrayBuilder()).add("members", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("id", "I1").add("name", "N1").add("description", "")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "I2").add("name", "N2").add("description", "")
                                        .add("online", false))
                                .add(Json.createObjectBuilder().add("id", "I3").add("name", "N3").add("description", "")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "123456").add("name", "Phil").add("description", "")
                                        .add("online", false))
                        )).build();
    }

    public JsonObject getServerIdFailure() {
        return Json.createObjectBuilder().add("status", "failure").add("message", "")
                .add("data", Json.createObjectBuilder()).build();
    }

    public JsonObject putUpdateMessageFailure() {
        return Json.createObjectBuilder().add("status", "failure").add("message", "You can not edit a chat message which do not belong to you")
                .add("data", Json.createObjectBuilder()).build();
    }

    public JsonObject deleteMessageFailure() {
        return Json.createObjectBuilder().add("status", "failure").add("message", "You can not delete a chat message which do not belong to you")
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

    public JsonObject getChannelMessage(Channel channel) {
        return Json.createObjectBuilder()
                .add(STATUS, SUCCESS)
                .add(MESSAGES, "")
                .add(DATA, Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add(ID, "message_id_1")
                                .add(CHANNEL, channel.getId())
                                .add(TIMESTAMP, 1616935874)
                                .add(FROM, "Bob")
                                .add(TEXT, "Hello there!"))
                        .add(Json.createObjectBuilder()
                                .add(ID, "message_id_2")
                                .add(CHANNEL, channel.getId())
                                .add(TIMESTAMP, 1616935884)
                                .add(FROM, "Bob")
                                .add(TEXT, "I am Bob")))
                .build();
    }

    public JsonObject getChannelMessageReference(Channel channel) {
        return Json.createObjectBuilder()
                .add(ID, "message_id_1")
                .add(CHANNEL, channel.getId())
                .add(TIMESTAMP, 1616935874)
                .add(FROM, "Bob")
                .add(TEXT, "#channelName2 1#channelName2 1")
                .build();
    }

    public JsonObject getChannelMessagesFailure() {
        return Json.createObjectBuilder()
                .add(STATUS, FAILURE)
                .add(MESSAGES, "Error")
                .add(DATA, Json.createArrayBuilder())
                .build();
    }

    private JsonObject build50Messages(Channel channel) {
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add(STATUS, SUCCESS)
                .add(MESSAGES, "");
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (int i = 1; i < 51; i++) {
            jsonArray.add(Json.createObjectBuilder()
                    .add(ID, "m_i_" + i)
                    .add(CHANNEL, channel.getId())
                    .add(TIMESTAMP, 1616936874 + i * 10)
                    .add(FROM, "Bob")
                    .add(TEXT, String.valueOf(i)));
        }
        return json.add(DATA, jsonArray).build();
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

    public JsonObject getTestMessageServerAnswer(JsonObject test_message, long timestamp) {
        return Json.createObjectBuilder()
                .add("id", "5e2ffbd8770dd077d03dr458")
                .add("channel", "idTest1")
                .add("timestamp", timestamp)
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

    public JsonObject getCategoryAudioChannels(JsonArray audioMembers) {
        return Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "idTest1")
                                .add("name", "channelName1")
                                .add("type", "audio")
                                .add("privileged", false)
                                .add("category", "categoryId1")
                                .add("members", Json.createArrayBuilder())
                                .add("audioMembers", audioMembers))).build();
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

    public JsonObject buildPrivateChatMessage(String to, String message) {
        return Json.createObjectBuilder()
                .add(CHANNEL, PRIVATE)
                .add(TO, to)
                .add(MESSAGE, message)
                .add(TIMESTAMP, 1234567)
                .add(FROM, localUser.getName())
                .build();
    }

    public JsonObject joinServer() {
        return Json.createObjectBuilder().add("status", "success").add("message", "Successfully arrived at server")
                .add("data", Json.createObjectBuilder()).build();
    }

    public JsonObject getNewServerSuccessful() {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder().add("id", "5e2ffbd8770dd077d03df505")
                        .add("name", "new Server").add("owner", "ow12ner").add("categories",
                                Json.createArrayBuilder()).add("members", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("id", "I1").add("name", "N1").add("description", "")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "I2").add("name", "N2").add("description", "")
                                        .add("online", false))
                                .add(Json.createObjectBuilder().add("id", "I3").add("name", "N3").add("description", "")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", localUser.getId()).add("name", localUser.getName()).add("description", "")
                                        .add("online", false))
                        )).build();
    }
}
