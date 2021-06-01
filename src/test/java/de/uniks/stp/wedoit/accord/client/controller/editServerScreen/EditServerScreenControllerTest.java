package de.uniks.stp.wedoit.accord.client.controller.editServerScreen;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
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
import javax.json.JsonObject;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.DATA;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.LINK;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EditServerScreenControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager stageManager;
    private LocalUser localUser;
    private Server server;
    @Mock
    private RestClient restMock;
    @Mock
    private HttpResponse<JsonNode> res;
    @Mock
    WebSocketClient webSocketClientMock;
    @Mock
    WebSocketClient chatWebSocketClientMock;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

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
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);

        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = stageManager.getEditor().haveLocalUser("Alice", "userKey123");
        this.server = stageManager.getEditor().haveServer(localUser, "id456", "AliceServer");
        this.stageManager.getEditor().getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClientMock);
        this.stageManager.getEditor().getNetworkController().haveWebSocket(CHAT_USER_URL + stageManager.getEditor().getNetworkController().getCleanLocalUserName()
                + AND_SERVER_ID_URL + this.server.getId(), chatWebSocketClientMock);

        this.stageManager.getEditor().getNetworkController().setRestClient(restMock);
        StageManager.showServerScreen(server);
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Mock the rest client's getServers method and create a callback
     *
     * @param restClientJson JsonObject, which one should return from the rest client as JsonNode
     */
    public void mockRest(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock).getExplicitServerInformation(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    @Test
    public void editServerScreenOpens() {
        localUser.setId("owner123");
        Label labelServerName = lookup("#lbServerName").query();
        Assert.assertEquals(labelServerName.getText(), server.getName());

        clickOn("#btnEdit");

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", stageManager.getPopupStage().getTitle());
    }

    @Test
    public void editServerScreenOpensAsAdmin() {
        localUser.setId("owner123");
        JsonObject jsonObject = buildServerInformationWithTwoMembers();
        mockRest(jsonObject);

        clickOn("#btnEdit");

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", stageManager.getPopupStage().getTitle());

        // Assert that Pop-Up Window shows correct widgets
        VBox vBoxAdminOnly = lookup("#vBoxAdminOnly").query();
        RadioButton radioBtnTemporal = lookup("#radioBtnTemporal").query();
        RadioButton radioBtnMaxCount = lookup("#radioBtnMaxCount").query();
        TextField tfMaxCountAmountInput = lookup("#tfMaxCountAmountInput").query();
        Button btnCreateInvitation = lookup("#btnCreateInvitation").query();
        TextField tfInvitationLink = lookup("#tfInvitationLink").query();
        Button btnDelete = lookup("#btnDelete").query();

        Assert.assertFalse(vBoxAdminOnly.isDisabled());
        Assert.assertFalse(radioBtnTemporal.isDisabled());
        Assert.assertFalse(radioBtnMaxCount.isDisabled());
        Assert.assertFalse(btnCreateInvitation.isDisabled());
        Assert.assertFalse(tfInvitationLink.isDisabled());
        Assert.assertFalse(btnDelete.isDisabled());

        // Assert Radiobutton switch works properly
        Assert.assertTrue(radioBtnMaxCount.isSelected());
        Assert.assertTrue(tfMaxCountAmountInput.isEditable());

        clickOn("#radioBtnTemporal");

        Assert.assertTrue(radioBtnTemporal.isSelected());
        Assert.assertFalse(radioBtnMaxCount.isSelected());
        Assert.assertFalse(tfMaxCountAmountInput.isEditable());

        clickOn("#radioBtnMaxCount");

    }

    @Test
    public void editServerScreenOpensAsMember() {
        localUser.setId("alice123");
        JsonObject jsonObject = buildServerInformationWithTwoMembers();
        mockRest(jsonObject);

        clickOn("#btnEdit");

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", stageManager.getPopupStage().getTitle());

        // Assert that Pop-Up Window shows correct widgets and that the vBoxAdmin is deleted
        VBox mainVBox = lookup("#mainVBox").query();
        Button btnDelete = lookup("#btnDelete").query();

        Assert.assertEquals(mainVBox.getChildren().size(), 2);
        Assert.assertTrue(btnDelete.isDisabled());
        Assert.assertFalse(btnDelete.isVisible());
    }

    @Test
    public void createCountInvitationSuccessful() {
        localUser.setId("owner123");
        mockRest(buildServerInformationWithTwoMembers());
        clickOn("#btnEdit");
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(buildInvitationSuccessful().toString()));

        RadioButton radioBtnMaxCount = lookup("#radioBtnMaxCount").query();
        TextField tfMaxCountAmountInput = lookup("#tfMaxCountAmountInput").query();
        TextField tfInvitationLink = lookup("#tfInvitationLink").query();

        clickOn(radioBtnMaxCount);
        tfMaxCountAmountInput.setText("15");
        clickOn("#btnCreateInvitation");
        WaitForAsyncUtils.waitForFxEvents();

        verify(restMock).createInvite(anyInt(), anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(buildInvitationSuccessful().getJsonObject(DATA).getString(LINK), tfInvitationLink.getText());
        Assert.assertEquals("Amount", tfMaxCountAmountInput.getPromptText());

    }

    @Test
    public void createTemporalInvitationSuccessful() {
        localUser.setId("owner123");
        mockRest(buildServerInformationWithTwoMembers());
        clickOn("#btnEdit");
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(buildInvitationSuccessful().toString()));

        RadioButton radioBtnTemporal = lookup("#radioBtnTemporal").query();
        TextField tfInvitationLink = lookup("#tfInvitationLink").query();

        clickOn(radioBtnTemporal);
        clickOn("#btnCreateInvitation");
        WaitForAsyncUtils.waitForFxEvents();

        verify(restMock).createInvite(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(buildInvitationSuccessful().getJsonObject(DATA).getString(LINK), tfInvitationLink.getText());

    }

    @Test
    public void createInvitationTestFailure() {
        localUser.setId("owner123");
        mockRest(buildServerInformationWithTwoMembers());
        clickOn("#btnEdit");
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(buildInvitationFailure().toString()));

        RadioButton radioBtnTemporal = lookup("#radioBtnTemporal").query();
        RadioButton radioBtnMaxCount = lookup("#radioBtnMaxCount").query();
        TextField tfMaxCountAmountInput = lookup("#tfMaxCountAmountInput").query();
        TextField tfInvitationLink = lookup("#tfInvitationLink").query();

        clickOn(radioBtnMaxCount);
        clickOn("#btnCreateInvitation");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Insert Amount > 0", tfMaxCountAmountInput.getPromptText());

        clickOn(radioBtnMaxCount);
        tfMaxCountAmountInput.setText("b");
        clickOn("#btnCreateInvitation");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Insert Amount > 0", tfMaxCountAmountInput.getPromptText());

        tfMaxCountAmountInput.setText("0");
        clickOn("#btnCreateInvitation");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Insert Amount > 0", tfMaxCountAmountInput.getPromptText());

        clickOn(radioBtnMaxCount);
        tfMaxCountAmountInput.setText("15");
        clickOn("#btnCreateInvitation");
        WaitForAsyncUtils.waitForFxEvents();

        verify(restMock).createInvite(anyInt(), anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("generation failed", tfInvitationLink.getPromptText());

        clickOn(radioBtnTemporal);
        clickOn("#btnCreateInvitation");

        verify(restMock).createInvite(anyString(), anyString(), callbackArgumentCaptor.capture());

        callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("generation failed", tfInvitationLink.getPromptText());

    }

    public JsonObject buildInvitationSuccessful() {
        return Json.createObjectBuilder().add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", "invitationId")
                        .add("link", "https://ac.uniks.de/api/...invitationId")
                        .add("type", "temporal")
                        .add("max", -1)
                        .add("current", -1)
                        .add("server", "serverId")
                ).build();
    }

    public JsonObject buildInvitationFailure() {
        return Json.createObjectBuilder().add("status", "failure")
                .add("message", "")
                .add("data", Json.createObjectBuilder()).build();
    }


    public JsonObject buildServerInformationWithTwoMembers() {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", server.getId())
                        .add("name", server.getName())
                        .add("owner", "owner123")
                        .add("categories", Json.createArrayBuilder())
                        .add("members", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("id", "alice123").add("name", "Alice")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "bob123").add("name", "Bob")
                                        .add("online", true))))
                .build();
    }

    @Override
    public void stop() {
        stage = null;
        stageManager = null;
        localUser = null;
        server = null;
        restMock = null;
        res = null;
        webSocketClientMock = null;
        chatWebSocketClientMock = null;
        callbackArgumentCaptor = null;
        rule = null;
    }

}
