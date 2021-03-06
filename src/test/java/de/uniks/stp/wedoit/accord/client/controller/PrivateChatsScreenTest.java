package de.uniks.stp.wedoit.accord.client.controller;


import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.PrivateChatController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.richtext.RichTextArea;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.EmojiButton;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
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

import javax.json.Json;
import javax.json.JsonObject;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.PRIVATE_CHATS_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_CLOSE;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_INVITE;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.PRIVATE_USER_CHAT_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.Network.SYSTEM_SOCKET_URL;
import static de.uniks.stp.wedoit.accord.client.constants.UserDescription.CUSTOM_KEY;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PrivateChatsScreenTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private Stage stage;
    private Stage emojiPickerStage;
    private StageManager stageManager;
    private LocalUser localUser;
    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;
    @Mock
    private WebSocketClient systemWebSocketClient;
    @Mock
    private WebSocketClient chatWebSocketClient;

    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentSystemCaptorWebSocket;

    private Editor editor;
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

        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);
        this.stageManager.initView(ControllerEnum.LOGIN_SCREEN, true, null);

        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
        emojiPickerStage = stageManager.getStage(StageEnum.EMOJI_PICKER_STAGE);
    }

    @Override
    public void stop() {
        rule = null;
        stage = null;
        stageManager.stop();
        stageManager = null;
        localUser = null;
        restMock = null;
        res = null;
        emojiPickerStage = null;
        callbackArgumentCaptor = null;
        systemWebSocketClient = null;
        chatWebSocketClient = null;
        callbackArgumentSystemCaptorWebSocket = null;
        editor = null;
    }

    @Test
    public void initUserListView() {
        directToPrivateChatsScreen();

        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);

        ListView<User> userListView = lookup("#lwOnlineUsers").queryListView();

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(3, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());

        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(1)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(2)));
    }

    @Test
    public void newUserOnlineListViewUpdated() {
        directToPrivateChatsScreen();

        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        ListView<User> userListView = lookup("#lwOnlineUsers").queryListView();
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(3, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());

        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(1)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(2)));

        mockSystemWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(4, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());

        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(1)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(2)));
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(3)));
    }

    @Test
    public void userLeftListViewUpdated() {
        directToPrivateChatsScreen();

        JsonObject restJson = getOnlineUsers();
        mockRest(restJson);

        ListView<User> userListView = lookup("#lwOnlineUsers").queryListView();

        JsonObject webSocketJsonUserJoined = webSocketCallbackUserJoined();
        mockSystemWebSocket(webSocketJsonUserJoined);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(4, userListView.getItems().size());
        Assert.assertEquals(localUser.getUsers().size(), userListView.getItems().size());
        Assert.assertTrue(userListView.getItems().contains(localUser.getUsers().get(0)));

        JsonObject webSocketJsonUserLeft = webSocketCallbackUserLeft();
        mockSystemWebSocket(webSocketJsonUserLeft);
        WaitForAsyncUtils.waitForFxEvents();


        Assert.assertEquals(3, userListView.getItems().size());
        Assert.assertEquals(stageManager.getEditor().getOnlineUsers().size(), userListView.getItems().size());
    }

    @Test
    public void testGameInvite() {
        initUserListView();

        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();
        Button btnPlay = lookup("#btnPlay").queryButton();

        Assert.assertEquals(LanguageResolver.getString("PLAY"), btnPlay.getText());

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        clickOn(btnPlay);
        WaitForAsyncUtils.waitForFxEvents();

        //send message
        JsonObject gameInvite = JsonUtil.buildPrivateChatMessage(user.getName(), GAME_INVITE);
        mockChatWebSocket(getTestMessageServerAnswer(gameInvite));
        WaitForAsyncUtils.waitForFxEvents();

        //no more message is send
        clickOn(btnPlay);
        WaitForAsyncUtils.waitForFxEvents();


        int lwNewestItem = lwPrivateChat.getItems().size() - 1;
        Assert.assertEquals(1, localUser.getGameRequests().size());
        Assert.assertEquals(0, localUser.getGameInvites().size());
        Assert.assertEquals(localUser.getName(), lwPrivateChat.getItems().get(lwNewestItem).getFrom());
        Assert.assertEquals(user.getName(), lwPrivateChat.getItems().get(lwNewestItem).getTo());
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(LanguageResolver.getString("SEND_GAME_INVITE"), lwPrivateChat.getItems().get(lwNewestItem).getText());


        //receive game accepted message
        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_INVITE));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(this.stageManager.getStage(StageEnum.GAME_STAGE).isShowing());
        Assert.assertEquals("Rock - Paper - Scissors", this.stageManager.getStage(StageEnum.GAME_STAGE).getTitle());
    }

    @Test
    public void testGameAccept() {
        //init user list and select first user
        initUserListView();
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();
        Button btnPlay = lookup("#btnPlay").queryButton();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        //receive invite message
        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_INVITE));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(1, localUser.getGameInvites().size());
        Assert.assertEquals(0, localUser.getGameRequests().size());
        int lwNewestItem = lwPrivateChat.getItems().size() - 1;
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(user.getName() + LanguageResolver.getString("RECEIVE_GAME_INVITE"), lwPrivateChat.getItems().get(lwNewestItem).getText());

        clickOn(btnPlay);

        //send message
        JsonObject gameAccept = JsonUtil.buildPrivateChatMessage(user.getName(), GAME_INVITE);
        mockChatWebSocket(getTestMessageServerAnswer(gameAccept));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(List.of(), localUser.getGameRequests());
        Assert.assertEquals(List.of(), localUser.getGameInvites());
        Assert.assertEquals(0, localUser.getGameInvites().size());
        Assert.assertTrue(this.stageManager.getStage(StageEnum.GAME_STAGE).isShowing());
        Assert.assertEquals("Rock - Paper - Scissors", this.stageManager.getStage(StageEnum.GAME_STAGE).getTitle());

    }

    @Test
    public void testQuitGame() {
        initUserListView();

        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();
        Button btnPlay = lookup("#btnPlay").queryButton();

        Assert.assertEquals(LanguageResolver.getString("PLAY"), btnPlay.getText());

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        clickOn(btnPlay);
        WaitForAsyncUtils.waitForFxEvents();

        //send invite
        JsonObject gameInvite = JsonUtil.buildPrivateChatMessage(user.getName(), GAME_INVITE);
        mockChatWebSocket(getTestMessageServerAnswer(gameInvite));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(localUser.getGameRequests().size(), 1);

        //send quit
        JsonObject gameQuit = JsonUtil.buildPrivateChatMessage(user.getName(), GAME_CLOSE);
        mockChatWebSocket(getTestMessageServerAnswer(gameQuit));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(localUser.getGameRequests().size(), 0);

        //receive game invite message
        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_INVITE));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(localUser.getGameInvites().size(), 1);

        //receive quit
        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_CLOSE));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(localUser.getGameInvites().size(), 0);

        // go inGame now
        clickOn(btnPlay);
        WaitForAsyncUtils.waitForFxEvents();

        //send message
        mockChatWebSocket(getTestMessageServerAnswer(gameInvite));
        WaitForAsyncUtils.waitForFxEvents();

        //add more gameInvites and Request to localUser so that the test covers all scenarios
        lwOnlineUsers.getSelectionModel().select(1);
        User user2 = lwOnlineUsers.getSelectionModel().getSelectedItem();
        lwOnlineUsers.getSelectionModel().select(2);
        User user3 = lwOnlineUsers.getSelectionModel().getSelectedItem();
        localUser.withGameRequests(user2);
        localUser.withGameInvites(user3);

        //receive game accepted message
        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_INVITE));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(this.stageManager.getStage(StageEnum.GAME_STAGE).isShowing());
        Assert.assertEquals("Rock - Paper - Scissors", this.stageManager.getStage(StageEnum.GAME_STAGE).getTitle());

        // opponent sends quit message --> go to result screen
        mockChatWebSocket(getServerMessageUserAnswer(user, GAME_CLOSE));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertTrue(this.stageManager.getStage(StageEnum.GAME_STAGE).isShowing());
        Assert.assertEquals("Result", this.stageManager.getStage(StageEnum.GAME_STAGE).getTitle());
        Label lbOutcome = lookup("#lbOutcome").query();
        Assert.assertEquals(lbOutcome.getText(), LanguageResolver.getString("OPPONENT_LEFT"));
    }

    @Test
    public void testChatSendMessage() {
        //init user list and select first user
        initUserListView();
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        WaitForAsyncUtils.waitForFxEvents();
        Button btnEmoji = lookup("#btnEmoji").queryButton();
        clickOn(btnEmoji);

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(emojiPickerStage.isShowing());
        Assert.assertEquals("Emoji Picker", emojiPickerStage.getTitle());

        GridPane panelForEmojis = (GridPane) emojiPickerStage.getScene().getRoot().lookup("#panelForEmojis");
        EmojiButton emoji = (EmojiButton) panelForEmojis.getChildren().get(0);
        clickOn(emoji);

        //send message
        clickOn("#tfEnterPrivateChat");
        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText("*Test* Message");
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), "*Test* Message" + emoji.getText());
        mockChatWebSocket(getTestMessageServerAnswer(test_message));

        WaitForAsyncUtils.waitForFxEvents();
        int lwNewestItem = lwPrivateChat.getItems().size() - 1;

        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals("*Test* Message" + emoji.getText(), lwPrivateChat.getItems().get(lwNewestItem).getText());

        clickOn("#tfEnterPrivateChat");
        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText("*Te\\*st* Message");
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        test_message = JsonUtil.buildPrivateChatMessage(user.getName(), "*Te\\*st* Message" + emoji.getText());
        mockChatWebSocket(getTestMessageServerAnswer(test_message));

        WaitForAsyncUtils.waitForFxEvents();
        lwNewestItem = lwPrivateChat.getItems().size() - 1;

        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem), user.getPrivateChat().getMessages().get(1));
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem).getText(), user.getPrivateChat().getMessages().get(1).getText());
        Assert.assertEquals("*Te\\*st* Message" + emoji.getText(), lwPrivateChat.getItems().get(lwNewestItem).getText());
    }

    @Test
    public void descriptionTest() {
        //init user list and select first user
        initUserListView();
        this.stageManager.getEditor().getLocalUser().setId("test");
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        Label lblDescription = lookup("#lblDescription").query();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("", lblDescription.getText());
        Assert.assertEquals("Albert", lblSelectedUser.getText());

        mockSystemWebSocket(descriptionChangedMessage());
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("- new Description", lblDescription.getText());
        Assert.assertEquals("Albert", lblSelectedUser.getText());
    }


    @Test
    public void testImageMessage() {
        initUserListView();

        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        clickOn("#tfEnterPrivateChat");

        String message = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/45/Eopsaltria_australis_-_Mogo_Campground.jpg/1200px-Eopsaltria_australis_-_Mogo_Campground.jpg";
        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText(message);
        press(KeyCode.ENTER);


        WaitForAsyncUtils.waitForFxEvents();
        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), message);
        mockChatWebSocket(getTestMessageServerAnswer(test_message));

        WaitForAsyncUtils.waitForFxEvents();
        int lwNewestItem = lwPrivateChat.getItems().size() - 1;
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(message, lwPrivateChat.getItems().get(lwNewestItem).getText());
    }

    @Test
    public void testGifMediaMessage() {
        initUserListView();

        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        clickOn("#tfEnterPrivateChat");

        String message = "https://media.giphy.com/media/AibXOtCbZgwChaOWcz/source.gif";
        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText(message);
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), message);
        mockChatWebSocket(getTestMessageServerAnswer(test_message));

        WaitForAsyncUtils.waitForFxEvents();
        int lwNewestItem = lwPrivateChat.getItems().size() - 1;
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals(message, lwPrivateChat.getItems().get(lwNewestItem).getText());
    }

    @Test
    public void testMp4MediaMessage() {
        initUserListView();

        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        clickOn("#tfEnterPrivateChat");

        String message = "https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1920_18MG.mp4";
        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText(message);
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), message);
        mockChatWebSocket(getTestMessageServerAnswer(test_message));

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(lwPrivateChat.getChildrenUnmodifiable().size(), 1);
    }


    @Test
    public void testYtVideoMessage() {
        initUserListView();

        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        clickOn("#tfEnterPrivateChat");

        String message = "https://youtu.be/NxvQPzrg2Wg";
        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText(message);
        press(KeyCode.ENTER);


        WaitForAsyncUtils.waitForFxEvents();
        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), message);
        mockChatWebSocket(getTestMessageServerAnswer(test_message));

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(lwPrivateChat.getChildrenUnmodifiable().size(), 1);
    }

    @Test
    public void testChatIncomingMessage() {
        initUserListView();
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        //receive message
        mockChatWebSocket(getServerMessageUserAnswer(user, "Hallo"));
        WaitForAsyncUtils.waitForFxEvents();

        int lastLwIndex = lwPrivateChat.getItems().size() - 1;
        System.err.println(lastLwIndex);
        Assert.assertEquals(lwPrivateChat.getItems().get(lastLwIndex), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(lastLwIndex).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals("Hallo", lwPrivateChat.getItems().get(lastLwIndex).getText());
    }

    @Test
    public void testChatIncomingMessageNoUserSelected() {
        initUserListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        User user = lwOnlineUsers.getItems().get(0);


        //receive message
        mockChatWebSocket(getServerMessageUserAnswer(user, "Hallo"));
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertFalse(lwOnlineUsers.getItems().get(0).isChatRead());
    }

    @Test
    public void testChatNoUserSelected() {
        //init user list and select first user
        initUserListView();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();

        //send message
        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText("Test Message\n");

        Assert.assertEquals(0, lwPrivateChat.getItems().size());
    }

    @Test
    public void testChatMessagesCachedProperlyAfterChatChange() {
        //init user list and select first user
        initUserListView();
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();

        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());

        //send message
        clickOn("#tfEnterPrivateChat");
        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText("Test Message");

        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();

        int lastLwIndex = lwPrivateChat.getItems().size() - 1;
        Assert.assertEquals(lwPrivateChat.getItems().get(lastLwIndex), user.getPrivateChat().getMessages().get(0));
        Assert.assertEquals(lwPrivateChat.getItems().get(lastLwIndex).getText(), user.getPrivateChat().getMessages().get(0).getText());
        Assert.assertEquals("Test Message", lwPrivateChat.getItems().get(lastLwIndex).getText());
    }

    public void mockRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock, atLeastOnce()).getOnlineUsers(anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockRestClient(JsonObject json) {
        when(res.getBody()).thenReturn(new JsonNode(json.toString()));

        verify(restMock).getServers(anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockChatWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(chatWebSocketClient).setCallback(callbackArgumentSystemCaptorWebSocket.capture());
        WSCallback wsSystemCallback = callbackArgumentSystemCaptorWebSocket.getValue();

        wsSystemCallback.handleMessage(webSocketJson);
    }

    @Test
    public void RichTextAreaTest() {
        RichTextArea richTextArea = new RichTextArea();

        richTextArea.setPromptText("Test", true);
        Assert.assertEquals(richTextArea.getPromptText(), "Test");

        richTextArea.updateTextColor(true);
        Assert.assertEquals(true, richTextArea.isDarkmode());

        richTextArea.updateTextColor(false);
        Assert.assertEquals(false, richTextArea.isDarkmode());

        richTextArea.setPlaceholder(new Text("123"));
        Assert.assertEquals(richTextArea.getPromptText(), "");

    }

    @Test
    public void testQuote() {
        //init user list and select first user
        initUserListView();
        Label lblSelectedUser = lookup("#lblSelectedUser").query();
        ListView<PrivateMessage> lwPrivateChat = lookup("#lwPrivateChat").queryListView();
        ListView<User> lwOnlineUsers = lookup("#lwOnlineUsers").queryListView();


        lwOnlineUsers.getSelectionModel().select(0);
        User user = lwOnlineUsers.getSelectionModel().getSelectedItem();

        clickOn("#lwOnlineUsers");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(user.getName(), lblSelectedUser.getText());


        //send message
        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText("Test Message");
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        JsonObject test_message = JsonUtil.buildPrivateChatMessage(user.getName(), "Test Message");
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();
        mockChatWebSocket(getTestMessageServerAnswer(test_message));
        WaitForAsyncUtils.waitForFxEvents();


        lwPrivateChat.getSelectionModel().select(lwPrivateChat.getItems().size());
        rightClickOn("#lwPrivateChat");

        PrivateMessage selectedItem = lwPrivateChat.getSelectionModel().getSelectedItem();
        clickOn("- quote");
        WaitForAsyncUtils.waitForFxEvents();
        Button btnCancelQuote = lookup("#btnCancelQuote").query();
        PrivateChatsScreenController privateChatsScreenController = (PrivateChatsScreenController) stageManager.getControllerMap().get(PRIVATE_CHATS_SCREEN_CONTROLLER);
        PrivateChatController privateChatController = privateChatsScreenController.getPrivateChatController();

        String formatted = stageManager.getEditor().getMessageManager().getMessageFormatted(selectedItem);
        Assert.assertEquals(privateChatController.getQuotedText(), formatted);
        clickOn(btnCancelQuote);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(privateChatController.getQuotedText(), "");


        lwPrivateChat.getSelectionModel().select(lwPrivateChat.getItems().size() - 1);
        rightClickOn("#lwPrivateChat");

        selectedItem = lwPrivateChat.getSelectionModel().getSelectedItem();
        clickOn("- quote");
        WaitForAsyncUtils.waitForFxEvents();

        formatted = stageManager.getEditor().getMessageManager().getMessageFormatted(selectedItem);
        Assert.assertEquals(privateChatController.getQuotedText(), formatted);

        ((RichTextArea) lookup("#tfEnterPrivateChat").query()).setText("quote");
        clickOn("#tfEnterPrivateChat");
        press(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        JsonObject quote = JsonUtil.buildPrivateChatMessage(user.getName(), QUOTE_PREFIX + formatted + QUOTE_MESSAGE + "123" + QUOTE_SUFFIX);
        JsonObject quote_message = JsonUtil.buildPrivateChatMessage(user.getName(), "quote");
        mockChatWebSocket(getTestMessageServerAnswer(quote));
        mockChatWebSocket(getTestMessageServerAnswer(quote_message));
        WaitForAsyncUtils.waitForFxEvents();


        int lwNewestItem = lwPrivateChat.getItems().size() - 1;
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem - 1).getText(), QUOTE_PREFIX + formatted + QUOTE_MESSAGE + "123" + QUOTE_SUFFIX);
        Assert.assertEquals(lwPrivateChat.getItems().get(lwNewestItem).getText(), "quote");

        lwPrivateChat.getSelectionModel().select(lwNewestItem);
        Assert.assertEquals(lwPrivateChat.getSelectionModel().getSelectedItem(), lwPrivateChat.getItems().get(lwNewestItem));
    }

    public JsonObject getOnlineUsers() {
        return Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "12345")
                                .add("name", "Albert")
                                .add("description", ""))
                        .add(Json.createObjectBuilder()
                                .add("id", "5678")
                                .add("name", "Clemens")
                                .add("description", ""))
                        .add(Json.createObjectBuilder()
                                .add("id", "203040")
                                .add("name", "Dieter")
                                .add("description", "")))
                .build();
    }

    public JsonObject getTestMessageServerAnswer(JsonObject test_message) {
        return Json.createObjectBuilder()
                .add("channel", "private")
                .add("timestamp", 1614938)
                .add("message", test_message.getString(MESSAGE))
                .add("from", localUser.getName())
                .add("to", test_message.getString(TO))
                .build();
    }

    public JsonObject getServerMessageUserAnswer(User user, String message) {
        return Json.createObjectBuilder()
                .add("channel", "private")
                .add("timestamp", 1614938)
                .add("message", message)
                .add("from", user.getName())
                .add("to", localUser.getName())
                .build();
    }

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has joined
     */
    public JsonObject webSocketCallbackUserJoined() {
        return Json.createObjectBuilder()
                .add("action", "userJoined")
                .add("data", Json.createObjectBuilder()
                        .add("id", "123456")
                        .add("name", "Phil")
                        .add("description", ""))
                .build();
    }

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has left
     */
    public JsonObject webSocketCallbackUserLeft() {
        return Json.createObjectBuilder()
                .add("action", "userLeft")
                .add("data", Json.createObjectBuilder()
                        .add("id", "123456")
                        .add("name", "Phil")
                        .add("description", ""))
                .build();
    }


    public void mockSystemWebSocket(JsonObject webSocketJson) {
        // mock websocket
        verify(systemWebSocketClient).setCallback(callbackArgumentSystemCaptorWebSocket.capture());
        WSCallback wsSystemCallback = callbackArgumentSystemCaptorWebSocket.getValue();

        wsSystemCallback.handleMessage(webSocketJson);
    }

    public void directToPrivateChatsScreen() {
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

        ((TextField) lookup("#tfUserName").query()).setText(username);

        ((TextField) lookup("#pwUserPw").query()).setText(password);

        clickOn("#btnLogin");

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callbackLogin = callbackArgumentCaptor.getValue();
        callbackLogin.completed(res);

        this.localUser = stageManager.getEditor().getLocalUser();

        WaitForAsyncUtils.waitForFxEvents();
    }

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

    @Test
    public void testBtnLogout() {

        JsonObject json = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "Logged out")
                .add("data", "{}")
                .build();
        when(res.getBody()).thenReturn(new JsonNode(json.toString()));

        directToPrivateChatsScreen();

        // got to privateChats screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Private Chats", stage.getTitle());

        // testing logout button
        // first have to open optionScreen
        clickOn("#btnOptions");
        Assert.assertEquals("Options - Appearance", stage.getTitle());

        clickOn("#btnLogout");

        verify(restMock).logout(anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callbackLogout = callbackArgumentCaptor.getValue();
        callbackLogout.completed(res);

        Assert.assertEquals("success", res.getBody().getObject().getString("status"));

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Login", stage.getTitle());
    }


    @Test
    public void testBtnHome() {

        directToPrivateChatsScreen();

        // got to privateChats screen
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Private Chats", stage.getTitle());

        // testing home button
        clickOn("#btnHome");

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Private Chats", stage.getTitle());
    }

    @Test
    public void testOnlineUserListViewInit() {

        directToPrivateChatsScreen();

        String returnMessage = Json.createObjectBuilder()
                .add("status", "success").add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df505")
                                .add("name", "Albert")
                                .add("description", "")
                        )
                        .add(Json.createObjectBuilder()
                                .add("id", "5e2ffbd8770dd077d03df506")
                                .add("name", "Clemens")
                                .add("description", ""))
                ).build().toString();
    }

    private JsonObject descriptionChangedMessage() {
        return Json.createObjectBuilder().add("action", USER_DESCRIPTION_CHANGED)
                .add("data", Json.createObjectBuilder().add("id", "12345").add("description", JsonUtil.buildDescription(CUSTOM_KEY, "new Description"))).build();
    }
}
