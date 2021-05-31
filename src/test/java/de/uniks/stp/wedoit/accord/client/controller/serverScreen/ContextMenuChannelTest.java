package de.uniks.stp.wedoit.accord.client.controller.serverScreen;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;

import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.AND_SERVER_ID_URL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ContextMenuChannelTest extends ApplicationTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    WebSocketClient webSocketClient;
    @Mock
    WebSocketClient chatWebSocketClient;
    @Mock
    WSCallback callback;
    private Stage stage;
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

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

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

        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = stageManager.getEditor().haveLocalUser("John_Doe", "testKey123");
        this.server = stageManager.getEditor().haveServer(localUser, "testId", "TServer");
        this.stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClient);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(CHAT_USER_URL + stageManager.getEditor().getNetworkController().clearUsername()
                + AND_SERVER_ID_URL + this.server.getId(), chatWebSocketClient);

        this.stageManager.getEditor().getNetworkController().setRestClient(restMock);
        StageManager.showServerScreen(server);

        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() {
        stage = null;
        stageManager = null;
        localUser = null;
        server = null;
    }

    public void mockChannelRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).getChannels(anyString(), anyString(), anyString(), channelCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = channelCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockCategoryRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).createCategory(anyString(), anyString(), anyString(), categoriesCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = categoriesCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    @Test
    public void createCategoryTest(){
        Platform.runLater(StageManager::showCreateCategoryScreen);
        WaitForAsyncUtils.waitForFxEvents();
        Button button = (Button) lookup("#btnCreateCategory").query();
        Assert.assertEquals(button.getText(), "Create");

        TextField textField = (TextField) lookup("#tfCategoryName").query();
        textField.setText("testCategory");
        clickOn("#btnCreateCategory");

        JsonObject json = buildCreateCategory("123", "testCategory", this.server.getId());
        mockCategoryRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Category newCategory = null;
        for(Category cat : this.server.getCategories()){
            if(cat.getId().equals("123")){
                newCategory = cat;
            }
        }
        Assert.assertNotNull(newCategory);
        Assert.assertEquals(newCategory.getName(), "testCategory");
    }

    @Test
    public void createCategoryFailureTest(){
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
        mockCategoryRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(errorLabel.getText(), "Something went wrong while creating the category");
    }

    @Test
    public void createChannelScreenControlerTest(){
        Platform.runLater(() -> {
            StageManager.showCreateChannelScreen(new Category());
            Button button = (Button) lookup("#btnCreateChannel").query();
            Assert.assertEquals(button.getText(), "Create");
        });
    }

    @Test
    public void editChannelScreenControlerTest(){
        Platform.runLater(() -> {
            StageManager.showEditChannelScreen(new Channel());
            Button button = (Button) lookup("#btnEditChannel").query();
            Assert.assertEquals(button.getText(), "Save");
        });
    }

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

}
