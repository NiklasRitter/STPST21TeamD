package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioConnection;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
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
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.net.DatagramSocket;
import java.util.concurrent.TimeUnit;

import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AudioManagerTest extends ApplicationTest {

    private Stage stage;
    private StageManager stageManager;
    private LocalUser localUser;
    private Server server;
    private Options oldOptions;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private RestClient restMock;
    @Mock
    private WebSocketClient webSocketClient;
    @Mock
    private WebSocketClient chatWebSocketClient;
    @Mock
    private AudioConnection audioConnection;
    @Mock
    private HttpResponse<JsonNode> res;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> channelCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<WSCallback> callbackArgumentCaptorWebSocket;
    private WSCallback wsCallback;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.oldOptions = new Options();
        stageManager.getResourceManager().loadOptions(this.oldOptions);
        stageManager.getResourceManager().saveOptions(new Options().setRememberMe(false));
        stageManager.getResourceManager().saveOptions(new Options().setLanguage("en_GB"));

        this.stageManager.start(stage);
        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = this.stageManager.getEditor().haveLocalUser("JohnDoe", "testKey123");
        this.localUser.setPassword("secret").setId("123");
        this.server = this.stageManager.getEditor().haveServer(localUser, "testId", "TServer");
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(CHAT_USER_URL + this.stageManager.getEditor().
                getWebSocketManager().getCleanLocalUserName() + AND_SERVER_ID_URL + this.server.getId(), chatWebSocketClient);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);
        this.stageManager.initView(ControllerEnum.SERVER_SCREEN, server, null);
        this.stageManager.getEditor().getAudioManager().setAudioConnection(audioConnection);
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
        stageManager = null;
        localUser = null;
        server = null;
        restMock = null;
        res = null;
        audioConnection = null;
        callbackArgumentCaptor = null;
        channelCallbackArgumentCaptor = null;
        callbackArgumentCaptorWebSocket = null;
        wsCallback = null;
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

    public void mockAudioInit() {
        verify(audioConnection).startConnection("cranberry.uniks.de", 33100);
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

        verify(restMock, atLeastOnce()).getCategories(anyString(), anyString(), channelCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = channelCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockJoinAudio(JsonObject restClientJson) {
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));
        verify(restMock).joinAudioChannel(anyString(), anyString(), anyString(), anyString(), channelCallbackArgumentCaptor.capture());
        Callback<JsonNode> callback = channelCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockLeaveAudio(JsonObject restClientJson) {
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));
        verify(restMock).leaveAudioChannel(anyString(), anyString(), anyString(), anyString(), channelCallbackArgumentCaptor.capture());
        Callback<JsonNode> callback = channelCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    @Test
    public void joinAudioServerTest() {
        initUserListView();
        JsonArray audioMembers = Json.createArrayBuilder().add("I1").build();
        initAudioChannelListView(audioMembers);
        TreeView<Object> treeView = lookup("#tvServerChannels").query();
        Assert.assertSame(treeView.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue(), server.getMembers().get(0));

        doubleClickOn("channelName1");
        JsonObject restClientJson = joinOrLeaveAudioChannel("I2", "idTest", "idTest1");
        audioMembers = Json.createArrayBuilder().add("I2").build();
        JsonObject channelRestJson = getCategoryAudioChannels(audioMembers);
        mockChannelRest(channelRestJson);
        WaitForAsyncUtils.waitForFxEvents();
        mockJoinAudio(restClientJson);
        mockAudioInit();
        WaitForAsyncUtils.waitForFxEvents();
        ObservableList<TreeItem<Object>> treeItems = treeView.getRoot().getChildren().get(0).getChildren().get(0).getChildren();
        User user = (User) treeItems.get(0).getValue();
        if (user.getId().equals("I1")) {
            user = (User) treeItems.get(1).getValue();
        }
        Assert.assertEquals("I2", user.getId());
        Assert.assertNotNull(stageManager.getEditor().getAudioManager().getAudioConnection());
    }

    @Test
    public void leaveAudioChannelTest() {
        initUserListView();
        JsonArray audioMembers = Json.createArrayBuilder().add("I1").build();
        initAudioChannelListView(audioMembers);
        localUser.setAudioChannel(server.getCategories().get(0).getChannels().get(0));
        TreeView<Object> treeView = lookup("#tvServerChannels").query();

        doubleClickOn("channelName1");

        JsonObject restClientJson = joinOrLeaveAudioChannel("I1", "idTest", "idTest1");
        audioMembers = Json.createArrayBuilder().build();
        JsonObject channelRestJson = getCategoryAudioChannels(audioMembers);
        mockChannelRest(channelRestJson);
        mockLeaveAudio(restClientJson);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(treeView.getRoot().getChildren().get(0).getChildren().get(0).getChildren().size(), 0);
        Assert.assertNull(stageManager.getEditor().getAudioManager().getAudioConnection());
    }

    @Test
    public void muteAndUnmuteUserAudioTest() {
        joinAudioServerTest();
        AudioManager audioManager = stageManager.getEditor().getAudioManager();
        Channel channel = stageManager.getEditor().getChannelById(server, "idTest", "idTest1");
        AudioConnection tempAudioCon = new AudioConnection(localUser, channel, stageManager.getEditor()) {
            @Override
            protected DatagramSocket createSocket() {
                DatagramSocket datagramSocket = null;
                try {
                    datagramSocket = new DatagramSocket(33100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return datagramSocket;
            }
        };

        audioManager.setAudioConnection(tempAudioCon);
        tempAudioCon.startConnection("localhost", 33100);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        User user = stageManager.getEditor().getUser("N1");
        audioManager.muteUser(user);
        Assert.assertTrue(user.isMuted());
        audioManager.unmuteUser(user);
        Assert.assertFalse(user.isMuted());
        tempAudioCon.stop();
    }

    @Test
    public void muteAndUnmuteAllTest() {
        joinAudioServerTest();
        Channel channel = stageManager.getEditor().getChannelById(server, "idTest", "idTest1");
        AudioConnection tempAudioCon = new AudioConnection(localUser, channel, stageManager.getEditor()) {
            @Override
            protected DatagramSocket createSocket() {
                DatagramSocket datagramSocket = null;
                try {
                    datagramSocket = new DatagramSocket(33100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return datagramSocket;
            }
        };
        stageManager.getEditor().getAudioManager().setAudioConnection(tempAudioCon);
        tempAudioCon.startConnection("localhost", 33100);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        clickOn("#btnMuteAll");
        for (User user : channel.getAudioMembers()) {
            if (!user.getName().equals(localUser.getName())) {
                Assert.assertTrue(user.isMuted());
            }
        }
        clickOn("#btnMuteAll");
        for (User user : channel.getAudioMembers()) {
            if (!user.getName().equals(localUser.getName())) {
                Assert.assertFalse(user.isMuted());
            }
        }
        tempAudioCon.stop();
    }

    @Test
    public void muteAndUnmuteYourselfTest() {
        joinAudioServerTest();
        Channel channel = stageManager.getEditor().getChannelById(server, "idTest", "idTest1");
        AudioConnection tempAudioCon = new AudioConnection(localUser, channel, stageManager.getEditor()) {
            @Override
            protected DatagramSocket createSocket() {
                DatagramSocket datagramSocket = null;
                try {
                    datagramSocket = new DatagramSocket(33100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return datagramSocket;
            }
        };
        stageManager.getEditor().getAudioManager().setAudioConnection(tempAudioCon);
        tempAudioCon.startConnection("localhost", 33100);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        stageManager.getEditor().getAudioManager().muteYourself(localUser);
        Assert.assertTrue(localUser.isMuted());
        stageManager.getEditor().getAudioManager().unmuteYourself(localUser);
        Assert.assertFalse(localUser.isMuted());
        press(KeyCode.CONTROL, KeyCode.M).release(KeyCode.CONTROL, KeyCode.M);
        ;
        Assert.assertTrue(localUser.isMuted());
        press(KeyCode.CONTROL, KeyCode.M).release(KeyCode.CONTROL, KeyCode.M);
        Assert.assertFalse(localUser.isMuted());
        tempAudioCon.stop();
    }

    @Test
    public void changeUserVolume() {
        joinAudioServerTest();
        Channel channel = stageManager.getEditor().getChannelById(server, "idTest", "idTest1");
        AudioConnection tempAudioCon = new AudioConnection(localUser, channel, stageManager.getEditor()) {
            @Override
            protected DatagramSocket createSocket() {
                DatagramSocket datagramSocket = null;
                try {
                    datagramSocket = new DatagramSocket(33100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return datagramSocket;
            }
        };
        stageManager.getEditor().getAudioManager().setAudioConnection(tempAudioCon);
        tempAudioCon.startConnection("localhost", 33100);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        User user = stageManager.getEditor().getUser("N2");
        int audioVolume = user.getAudioVolume();
        user.setAudioVolume(100);
        tempAudioCon.getAudioReceive().updateVolume();
        Assert.assertNotEquals(audioVolume, user.getAudioVolume(), 0.0000);
        audioVolume = user.getAudioVolume();
        user.setAudioVolume(-100);
        tempAudioCon.getAudioReceive().updateVolume();
        Assert.assertNotEquals(audioVolume, user.getAudioVolume(), 0.0000);
    }

    @Test
    public void removeUserFromAudioChannelOfServerTest() {
        initUserListView();
        JsonArray audioMembers = Json.createArrayBuilder().add("I1").build();
        initAudioChannelListView(audioMembers);
        Server testServer = new Server().withCategories(new Category().withChannels(new Channel().setType("audio").withAudioMembers(new User())));
        Assert.assertEquals(testServer.getCategories().get(0).getChannels().get(0).getAudioMembers().size(), 1);
        stageManager.getEditor().removeUserFromAudioChannelOfServer(testServer);
        Assert.assertEquals(testServer.getCategories().get(0).getChannels().get(0).getAudioMembers().size(), 0);
    }


    public void initUserListView() {
        JsonObject restJson = getServerIdSuccessful();
        JsonObject webSocketJson = webSocketCallbackUserJoined();
        mockRest(restJson);
        mockWebSocket(webSocketJson);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void initAudioChannelListView(JsonArray audioMembers) {
        JsonObject categoriesRestJson = getServerCategories();
        mockGetCategoryRest(categoriesRestJson);
        WaitForAsyncUtils.waitForFxEvents();
        JsonObject channelRestJson = getCategoryAudioChannels(audioMembers);
        mockChannelRest(channelRestJson);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public JsonObject joinOrLeaveAudioChannel(String userId, String categoryId, String channelId) {
        return Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("id", userId)
                                .add("category", categoryId)
                                .add("channel", channelId))).build();
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

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has joined
     */
    public JsonObject webSocketCallbackUserJoined() {
        return Json.createObjectBuilder().add("action", "userJoined").add("data",
                Json.createObjectBuilder().add("id", "123456").add("name", "Phil").add("description", "")).build();
    }
}
