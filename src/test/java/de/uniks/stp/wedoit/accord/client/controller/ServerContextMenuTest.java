package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.AND_SERVER_ID_URL;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ServerContextMenuTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    WebSocketClient webSocketClient;
    @Mock
    WebSocketClient chatWebSocketClient;

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
    private ArgumentCaptor<Callback<JsonNode>> categoriesCallbackArgumentCaptor;
    private Options oldOptions;

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

        this.stageManager.start(stage);
        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = this.stageManager.getEditor().haveLocalUser("John_Doe", "testKey123");
        this.localUser.setId("123");
        this.server = this.stageManager.getEditor().haveServer(localUser, "testId", "TServer");
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClient);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(CHAT_USER_URL + this.stageManager.getEditor().
                getWebSocketManager().getCleanLocalUserName() + AND_SERVER_ID_URL + this.server.getId(), chatWebSocketClient);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);
        this.stageManager.showServerScreen(server);

        this.stage.centerOnScreen();
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
        callbackArgumentCaptor = null;
        channelsCallbackArgumentCaptor = null;
        categoriesCallbackArgumentCaptor = null;
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

    public void mockCreateCategoryRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).createCategory(anyString(), anyString(), anyString(), categoriesCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = categoriesCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockUpdateCategoryRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).updateCategory(anyString(), anyString(), anyString(), anyString(), categoriesCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = categoriesCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockDeleteCategoryRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).deleteCategory(anyString(), anyString(), anyString(), categoriesCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = categoriesCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockCreateChannelRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).createChannel(anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString(), channelsCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = channelsCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockUpdateChannelRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).updateChannel(anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(), anyString(), channelsCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = channelsCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockDeleteChannelRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).deleteChannel(anyString(), anyString(), anyString(), anyString(), channelsCallbackArgumentCaptor.capture());

        Callback<JsonNode> callback = channelsCallbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    @Test
    public void createCategoryTest() {
        Platform.runLater(() -> stageManager.showCreateCategoryScreen());
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnCreateCategory").query();
        Assert.assertEquals(button.getText(), "Create");

        TextField textField = lookup("#tfCategoryName").query();
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
        Platform.runLater(() -> stageManager.showCreateCategoryScreen());
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnCreateCategory").query();
        Assert.assertEquals(button.getText(), "Create");

        clickOn("#btnCreateCategory");
        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals(errorLabel.getText(), "Name has to be at least 1 symbols long");

        TextField textField = lookup("#tfCategoryName").query();
        textField.setText("testCategory");
        clickOn("#btnCreateCategory");

        JsonObject json = buildFailure();
        mockCreateCategoryRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(errorLabel.getText(), "Something went wrong while creating the category");
    }

    @Test
    public void editCategoryTest() {
        Category category = new Category().setId("12345").setName("someCategory");
        server.withCategories(category);
        Platform.runLater(() -> {
            this.stageManager.showEditCategoryScreen(category);
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnEditCategory").query();
        Assert.assertEquals(button.getText(), "Save");


        TextField textField = lookup("#tfCategoryName").query();
        Assert.assertEquals(textField.getText(), category.getName());
        textField.setText("categoryTest");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnEditCategory");

        JsonObject json = buildCreateCategory(category.getId(), textField.getText(), server.getId());
        mockUpdateCategoryRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(category.getName(), "categoryTest");
    }

    @Test
    public void editCategoryFailureTest() {
        Category category = new Category().setId("12345").setName("someCategory");
        category.setServer(server);
        Platform.runLater(() -> {
            this.stageManager.showEditCategoryScreen(category);
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnEditCategory").query();
        Assert.assertEquals(button.getText(), "Save");

        TextField textField = lookup("#tfCategoryName").query();
        textField.setText("");
        clickOn("#btnEditCategory");
        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals(errorLabel.getText(), "Name has to be at least 1 symbols long");

        textField.setText("testCategory");
        clickOn("#btnEditCategory");
        JsonObject json = buildFailure();
        mockUpdateCategoryRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(errorLabel.getText(), "Something went wrong while updating the category");
    }

    @Test
    public void deleteCategoryTest() {
        Category category = new Category().setId("12345").setName("someCategory");
        category.setServer(server);
        Platform.runLater(() -> {
            this.stageManager.showEditCategoryScreen(category);
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnDeleteCategory").query();
        Assert.assertEquals(button.getText(), "Delete");

        clickOn("#btnDeleteCategory");
        clickOn("#btnDiscard");
        Assert.assertEquals(category.getServer(), server);

        clickOn("#btnDeleteCategory");
        clickOn("#btnDelete");

        JsonObject json = buildCreateCategory(category.getId(), category.getId(), server.getId());
        mockDeleteCategoryRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertNull(category.getServer());
    }

    @Test
    public void deleteCategoryFailureTest() {
        Category category = new Category().setId("12345");
        category.setServer(server);
        Platform.runLater(() -> {
            this.stageManager.showEditCategoryScreen(category);
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnDeleteCategory").query();
        Assert.assertEquals(button.getText(), "Delete");

        clickOn("#btnDeleteCategory");
        clickOn("#btnDelete");

        JsonObject json = buildFailure();
        mockDeleteCategoryRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertNotNull(category.getServer());
    }

    @Test
    public void createChannelTest() {

        Category category = new Category().setId("12345");
        category.setServer(server);

        Platform.runLater(() -> {
            this.stageManager.showCreateChannelScreen(category);
        });
        WaitForAsyncUtils.waitForFxEvents();

        Button button = lookup("#btnCreateChannel").query();
        Assert.assertEquals(button.getText(), "Create");

        TextField textField = lookup("#tfChannelName").query();
        textField.setText("testChannel");
        clickOn("#btnCreateChannel");

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
        Assert.assertFalse(newChannel.isRead());
    }

    @Test
    public void createPrivilegedChannelTest() {
        JsonObject restJson = getServerIdSuccessful();
        mockRest(restJson);

        Category category = new Category().setId("12345");
        category.setServer(server);

        Platform.runLater(() -> {
            this.stageManager.showCreateChannelScreen(category);
        });
        WaitForAsyncUtils.waitForFxEvents();

        Button button = lookup("#btnCreateChannel").query();
        Assert.assertEquals(button.getText(), "Create");

        TextField textField = lookup("#tfChannelName").query();
        textField.setText("testChannel");

        CheckBox checkBoxPrivileged = lookup("#checkBoxPrivileged").query();
        clickOn(checkBoxPrivileged);
        WaitForAsyncUtils.waitForFxEvents();

        VBox vBoxMemberNameAndCheckBox = lookup("#vBoxMemberNameAndCheckBox").query();
        WaitForAsyncUtils.waitForFxEvents();

        WaitForAsyncUtils.waitForFxEvents();
        HBox hBoxPlaceHolder = (HBox) vBoxMemberNameAndCheckBox.getChildren().get(0);
        VBox vBoxLblMemberNames = (VBox) hBoxPlaceHolder.getChildren().get(0);
        VBox vBoxCheckBox = (VBox) hBoxPlaceHolder.getChildren().get(1);

        WaitForAsyncUtils.waitForFxEvents();
        Label lblMemberName = (Label) vBoxLblMemberNames.getChildren().get(0);

        WaitForAsyncUtils.waitForFxEvents();
        CheckBox checkBoxPrivilegedMember = (CheckBox) vBoxCheckBox.getChildren().get(0);
        clickOn(checkBoxPrivilegedMember);

        String memberId = "";
        for (User member : server.getMembers()) {
            if (member.getName().equals(lblMemberName.getText())) {
                memberId = member.getId();
            }
        }

        WaitForAsyncUtils.waitForFxEvents();
        Button btnCreate = lookup("#btnCreateChannel").query();
        Assert.assertEquals(btnCreate.getText(), "Create");
        clickOn(btnCreate);

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(stage.getTitle(), "Server");

        JsonArray members = Json.createArrayBuilder().add(memberId).build();
        JsonObject json = buildCreateChannel(category.getId(), "4321", "testChannel", "text", true, members);
        mockCreateChannelRest(json);

        WaitForAsyncUtils.waitForFxEvents();

        Channel newChannel = null;
        for (Channel channel : category.getChannels()) {
            if (channel.getId().equals("4321")) {
                newChannel = channel;
            }
        }

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertNotNull(newChannel);
        Assert.assertEquals(newChannel.getName(), "testChannel");
        Assert.assertEquals(newChannel.getMembers().get(0).getId(), server.getMembers().get(0).getId());
        Assert.assertEquals(newChannel.getMembers().get(0).getName(), lblMemberName.getText());
        Assert.assertFalse(newChannel.isRead());

    }

    @Test
    public void createChannelFailureTest() {

        Category category = new Category().setId("12345");
        Platform.runLater(() -> {
            this.stageManager.showCreateChannelScreen(category);
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnCreateChannel").query();
        Assert.assertEquals(button.getText(), "Create");

        clickOn("#btnCreateChannel");
        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals(errorLabel.getText(), "Name has to be at least 1 symbols long");

        TextField textField = lookup("#tfChannelName").query();
        textField.setText("testChannel");
        clickOn("#btnCreateChannel");

        JsonObject json = buildFailure();
        mockCreateChannelRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(errorLabel.getText(), "Something went wrong while creating the channel");
    }

    @Test
    public void editChannelTest() {
        Category category = new Category().setId("12345");
        Channel channel = new Channel().setId("54321").setName("test");
        channel.setCategory(category);
        server.withCategories(category);
        Platform.runLater(() -> {
            this.stageManager.showEditChannelScreen(channel);
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnSave").query();
        Assert.assertEquals(button.getText(), "Save");

        TextField textField = lookup("#tfChannelName").query();
        Assert.assertEquals(textField.getText(), channel.getName());
        textField.setText("channelTest");
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnSave");

        JsonArray members = Json.createArrayBuilder().build();
        JsonObject json = buildCreateChannel(category.getId(), channel.getId(), textField.getText(), "text", false, members);
        mockUpdateChannelRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(channel.getName(), "channelTest");
    }

    @Test
    public void editChannelPrivilegedTest() {
        JsonObject restJson = getServerIdSuccessful();
        mockRest(restJson);

        Category category = new Category().setId("12345");
        Channel channel = new Channel().setId("54321").setName("test");
        channel.setCategory(category);
        server.withCategories(category);

        Platform.runLater(() -> {
            this.stageManager.showEditChannelScreen(channel);
        });
        WaitForAsyncUtils.waitForFxEvents();

        Button button = lookup("#btnSave").query();
        Assert.assertEquals(button.getText(), "Save");

        TextField textField = lookup("#tfChannelName").query();
        Assert.assertEquals(textField.getText(), channel.getName());
        textField.setText("channelTest");

        CheckBox checkBoxPrivileged = lookup("#checkBoxPrivileged").query();
        clickOn(checkBoxPrivileged);
        WaitForAsyncUtils.waitForFxEvents();

        VBox vBoxMemberNameAndCheckBox = lookup("#vBoxMemberNameAndCheckBox").query();
        WaitForAsyncUtils.waitForFxEvents();

        HBox hBoxPlaceHolder = (HBox) vBoxMemberNameAndCheckBox.getChildren().get(0);
        VBox vBoxLblMemberNames = (VBox) hBoxPlaceHolder.getChildren().get(0);
        VBox vBoxCheckBox = (VBox) hBoxPlaceHolder.getChildren().get(1);

        WaitForAsyncUtils.waitForFxEvents();
        Label lblMemberName = (Label) vBoxLblMemberNames.getChildren().get(0);

        WaitForAsyncUtils.waitForFxEvents();
        CheckBox checkBoxPrivilegedMember = (CheckBox) vBoxCheckBox.getChildren().get(0);
        clickOn(checkBoxPrivilegedMember);

        String memberId = "";
        for (User member : server.getMembers()) {
            if (member.getName().equals(lblMemberName.getText())) {
                memberId = member.getId();
            }
        }

        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnSave");

        JsonArray members = Json.createArrayBuilder().add(memberId).build();
        JsonObject json = buildCreateChannel(category.getId(), channel.getId(), textField.getText(), "text", true, members);
        mockUpdateChannelRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(channel.getName(), "channelTest");
        Assert.assertTrue(channel.isPrivileged());
        Assert.assertEquals(channel.getMembers().get(0).getId(), memberId);

    }

    @Test
    public void editChannelFailureTest() {
        Category category = new Category().setId("12345");
        Channel channel = new Channel().setId("54321");
        channel.setCategory(category);
        Platform.runLater(() -> {
            this.stageManager.showEditChannelScreen(channel);
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnSave").query();
        Assert.assertEquals(button.getText(), "Save");

        TextField textField = lookup("#tfChannelName").query();
        textField.setText("");
        clickOn("#btnSave");
        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals(errorLabel.getText(), "Name has to be at least 1 symbols long");

        textField.setText("testChannel");
        clickOn("#btnSave");
        JsonObject json = buildFailure();
        mockUpdateChannelRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(errorLabel.getText(), "Something went wrong while updating the channel");
    }

    @Test
    public void deleteChannelTest() {
        Category category = new Category().setId("12345");
        category.setServer(server);
        Channel channel = new Channel().setId("54321").setName("testChannel");
        channel.setCategory(category);
        Platform.runLater(() -> {
            this.stageManager.showEditChannelScreen(channel);
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnDeleteChannel").query();
        Assert.assertEquals(button.getText(), "Delete");

        clickOn("#btnDeleteChannel");
        clickOn("#btnDiscard");
        Assert.assertEquals(channel.getCategory(), category);

        clickOn("#btnDeleteChannel");
        clickOn("#btnDelete");

        JsonArray members = Json.createArrayBuilder().build();
        JsonObject json = buildCreateChannel(category.getId(), channel.getId(), channel.getName(), "text", false, members);
        mockDeleteChannelRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertNull(channel.getCategory());
    }

    @Test
    public void deleteChannelFailureTest() {
        Category category = new Category().setId("12345");
        category.setServer(server);
        Channel channel = new Channel().setId("54321").setName("testChannel");
        channel.setCategory(category);
        Platform.runLater(() -> {
            this.stageManager.showEditChannelScreen(channel);
        });
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnDeleteChannel").query();
        Assert.assertEquals(button.getText(), "Delete");

        clickOn("#btnDeleteChannel");
        clickOn("#btnDelete");

        JsonObject json = buildFailure();
        mockDeleteChannelRest(json);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertNotNull(channel.getCategory());
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
     * create response when a channel is created
     */
    public JsonObject buildCreateChannel(String categoryId, String id, String channelName, String type, boolean privileged, JsonArray members) {
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
     * create response when a something goes wrong
     */
    public JsonObject buildFailure() {
        return Json.createObjectBuilder().add("status", "failure")
                .add("message", "")
                .add("data", Json.createObjectBuilder()).build();
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
                                        .add("online", true)))).build();
    }

}
