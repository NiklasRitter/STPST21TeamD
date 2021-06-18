package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
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

import javax.json.Json;

import static de.uniks.stp.wedoit.accord.client.constants.Network.PRIVATE_USER_CHAT_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.Network.SYSTEM_SOCKET_URL;
import static org.mockito.Mockito.*;

public class AutomatedLoginFailureTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private Stage stage;
    private StageManager stageManager;
    @Mock
    private RestClient restMock;
    @Mock
    private WebSocketClient systemWebSocketClient;
    @Mock
    private WebSocketClient chatWebSocketClient;
    @Mock
    private HttpResponse<JsonNode> res;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;
    private Options oldOptions;
    private LocalUser oldLocalUser;

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
        stageManager.getResourceManager().saveOptions(new Options().setRememberMe(true));
        oldLocalUser = new LocalUser();
        stageManager.getResourceManager().loadLocalUser(oldLocalUser);
        stageManager.getResourceManager().saveLocalUser(new LocalUser().setName("username").setPassword("123"));

        this.stageManager.getEditor().getNetworkController().haveWebSocket(SYSTEM_SOCKET_URL, systemWebSocketClient);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(PRIVATE_USER_CHAT_PREFIX + "username", chatWebSocketClient);

        this.stageManager.getEditor().getNetworkController().setRestClient(restMock);

        String returnMessage = Json.createObjectBuilder().add("status", "failure").add("message", "").add("data", Json.createObjectBuilder().add("userKey", "c653b568-d987-4331-8d62-26ae617847bf")).build().toString();
        when(res.getBody()).thenReturn(new JsonNode(returnMessage));
        this.stageManager.start(stage);
        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() {
        stageManager.getResourceManager().saveOptions(this.oldOptions);
        stageManager.getResourceManager().saveLocalUser(this.oldLocalUser);
        oldLocalUser = null;
        rule = null;
        stage = null;
        stageManager = null;
        restMock = null;
        systemWebSocketClient = null;
        chatWebSocketClient = null;
        res = null;
        callbackArgumentCaptor = null;

    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void startWithRememberMeOption() {
        Assert.assertEquals(stage.getTitle(), "Login");
        Assert.assertNull(stageManager.getEditor().getLocalUser().getName());
    }



}
