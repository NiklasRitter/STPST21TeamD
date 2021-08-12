package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioConnection;
import javafx.collections.ObservableList;
import javafx.scene.control.Slider;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import java.net.DatagramSocket;
import java.util.concurrent.TimeUnit;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.SERVER_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;
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
        stageManager.getResourceManager().loadOptions(oldOptions);
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
    }

    @Test
    public void HttpServerTest() {

        String CONTEXT = "/app";
        int PORT = 8000;

        // Create a new SimpleHttpServer
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(PORT, CONTEXT,
                new HttpRequestHandler());

        // Start the server
        simpleHttpServer.start();
        System.out.println("Server is started and listening on port "+ PORT);

    }
}
