package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioConnection;
import javafx.scene.control.TreeView;
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

import java.util.concurrent.TimeUnit;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.SERVER_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.AND_SERVER_ID_URL;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AudioManagerTest extends ApplicationTest {

    private Stage stage;
    private Stage emojiPickerStage;
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
        this.oldOptions = new Options();
        stageManager.getResourceManager().loadOptions(oldOptions);
        stageManager.getResourceManager().saveOptions(new Options().setRememberMe(false));
        stageManager.getResourceManager().saveOptions(new Options().setLanguage("en_GB"));

        this.stageManager.start(stage);
        this.emojiPickerStage = this.stageManager.getEmojiPickerStage();
        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = this.stageManager.getEditor().haveLocalUser("JohnDoe", "testKey123");
        this.localUser.setPassword("secret").setId("123");
        this.server = this.stageManager.getEditor().haveServer(localUser, "testId", "TServer");
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(CHAT_USER_URL + this.stageManager.getEditor().
                getWebSocketManager().getCleanLocalUserName() + AND_SERVER_ID_URL + this.server.getId(), chatWebSocketClient);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);
        this.stageManager.initView(STAGE, "Server", "ServerScreen", SERVER_SCREEN_CONTROLLER, true, server, null);
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
        emojiPickerStage = null;
        stageManager.stop();
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

    public void mockAudioInit(){
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

    public void mockJoinAudio(JsonObject restClientJson){
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));
        verify(restMock).joinAudioChannel(anyString(), anyString(), anyString(), anyString(), channelCallbackArgumentCaptor.capture());
        Callback<JsonNode> callback = channelCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockLeaveAudio(JsonObject restClientJson){
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));
        verify(restMock).leaveAudioChannel(anyString(), anyString(), anyString(), anyString(), channelCallbackArgumentCaptor.capture());
        Callback<JsonNode> callback = channelCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    @Test
    public void joinAudioServerTest(){
        initUserListView();
        JsonArray audioMembers = Json.createArrayBuilder().add("I1").build();
        initAudioChannelListView(audioMembers);
        TreeView<Object> treeView = lookup("#tvServerChannels").query();
        Assert.assertSame(treeView.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue(), server.getMembers().get(0));

        doubleClickOn("channelName1");
        JsonObject restClientJson = joinOrLeaveAudioChannel("I2", "idTest", "idTest1");
        audioMembers = Json.createArrayBuilder().add("I1").add("I2").build();
        JsonObject channelRestJson = getCategoryAudioChannels(audioMembers);
        mockChannelRest(channelRestJson);
        WaitForAsyncUtils.waitForFxEvents();
        mockJoinAudio(restClientJson);
        mockAudioInit();
        WaitForAsyncUtils.waitForFxEvents();
        User user = (User) treeView.getRoot().getChildren().get(0).getChildren().get(0).getChildren().get(1).getValue();
        Assert.assertEquals(user.getId(), "I2");
        Assert.assertNotNull(stageManager.getEditor().getAudioManager().getAudioConnection());
    }

    @Test
    public void leaveAudioChannelTest(){
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
    public void muteAndUnmuteUserAudioTest(){
        joinAudioServerTest();
        Channel channel = stageManager.getEditor().getChannelById(server, "idTest", "idTest1");
        AudioConnection tempAudioCon = new AudioConnection(localUser, channel);
        stageManager.getEditor().getAudioManager().setAudioConnection(tempAudioCon);
        tempAudioCon.startConnection("localhost", 33100);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        User user = stageManager.getEditor().getUser("N1");
        stageManager.getEditor().getAudioManager().muteUser(user);
        Assert.assertTrue(user.isMuted());
        stageManager.getEditor().getAudioManager().unmuteUser(user);
        Assert.assertFalse(user.isMuted());
        tempAudioCon.close();
    }

    @Test
    public void muteAndUnmuteAllTest(){
        joinAudioServerTest();
        Channel channel = stageManager.getEditor().getChannelById(server, "idTest", "idTest1");
        AudioConnection tempAudioCon = new AudioConnection(localUser, channel);
        stageManager.getEditor().getAudioManager().setAudioConnection(tempAudioCon);
        tempAudioCon.startConnection("localhost", 33100);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        clickOn("#btnMuteAll");
        for(User user : channel.getAudioMembers()){
            if(!user.getName().equals(localUser.getName())){
                Assert.assertTrue(user.isMuted());
            }
        }
        tempAudioCon.close();
    }

    @Test
    public void muteAndUnmuteYourselfTest(){
        joinAudioServerTest();
        Channel channel = stageManager.getEditor().getChannelById(server, "idTest", "idTest1");
        AudioConnection tempAudioCon = new AudioConnection(localUser, channel);
        stageManager.getEditor().getAudioManager().setAudioConnection(tempAudioCon);
        tempAudioCon.startConnection("localhost", 33100);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        stageManager.getEditor().getAudioManager().muteYourself(localUser);
        Assert.assertTrue(localUser.isMuted());
        stageManager.getEditor().getAudioManager().unmuteYourself(localUser);
        Assert.assertFalse(localUser.isMuted());
        tempAudioCon.close();
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
                                .add(Json.createObjectBuilder().add("id", "I1").add("name", "N1")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "I2").add("name", "N2")
                                        .add("online", false))
                                .add(Json.createObjectBuilder().add("id", "I3").add("name", "N3")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "123456").add("name", "Phil")
                                        .add("online", false))
                        )).build();
    }

    /**
     * @return Json webSocketCallback that user with id: "123456" and name: "Phil" has joined
     */
    public JsonObject webSocketCallbackUserJoined() {
        return Json.createObjectBuilder().add("action", "userJoined").add("data",
                Json.createObjectBuilder().add("id", "123456").add("name", "Phil")).build();
    }


}
