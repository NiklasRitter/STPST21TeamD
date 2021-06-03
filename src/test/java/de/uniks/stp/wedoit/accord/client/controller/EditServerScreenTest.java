package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
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
import static org.mockito.Mockito.*;

public class EditServerScreenTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    WebSocketClient webSocketClientMock;
    @Mock
    WebSocketClient chatWebSocketClientMock;
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
    public void mockRestExplicitServer(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock, atLeastOnce()).getExplicitServerInformation(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockRestChangeServerName(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock, atLeastOnce()).changeServerName(anyString(), anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    @Test
    public void editServerScreenOpens() {
        localUser.setId("owner123");
        JsonObject jsonObject = buildServerInformationWithTwoMembers();
        mockRestExplicitServer(jsonObject);
        Label labelServerName = lookup("#lbServerName").query();
        Assert.assertEquals(labelServerName.getText(), server.getName());

        clickOn("#btnEdit");
        WaitForAsyncUtils.waitForFxEvents();
        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", stageManager.getPopupStage().getTitle());
    }

    @Test
    public void editServerScreenOpensAsAdmin() {
        localUser.setId("owner123");
        JsonObject jsonObject = buildServerInformationWithTwoMembers();
        mockRestExplicitServer(jsonObject);

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
    public void editServerScreenNotVisibleForMember() {
        localUser.setId("alice123");
        JsonObject jsonObject = buildServerInformationWithTwoMembers();
        mockRestExplicitServer(jsonObject);

        Button btnEdit = lookup("#btnEdit").query();

        Assert.assertFalse(btnEdit.isVisible());
    }

    @Test
    public void changeServerNameSuccessful() {
        localUser.setId("owner123");
        JsonObject serverInfoJson = buildServerInformationWithTwoMembers();
        JsonObject serverChangeNameJson = buildServerNameChangeSuccessful("TestServerName");
        mockRestExplicitServer(serverInfoJson);

        Assert.assertEquals(server.getName(), "AliceServer");

        clickOn("#btnEdit");

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", stageManager.getPopupStage().getTitle());

        Label lblError = (Label) lookup("#lblError").query();

        ((TextField) lookup("#tfNewServernameInput").query()).setText("TestServerName");

        clickOn("#btnSave");

        mockRestChangeServerName(serverChangeNameJson);

        // needed, so that we can assert that new name is set correctly, and lblError is also set correctly
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(server.getName(), "TestServerName");
        Assert.assertFalse(lblError.isVisible());
    }

    @Test
    public void changeServerNameFail() {
        localUser.setId("owner123");
        JsonObject serverInfoJson = buildServerInformationWithTwoMembers();
        JsonObject serverChangeNameJson = buildServerNameChangeFailure();
        mockRestExplicitServer(serverInfoJson);

        Assert.assertEquals(server.getName(), "AliceServer");

        clickOn("#btnEdit");

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", stageManager.getPopupStage().getTitle());

        Label lblError = (Label) lookup("#lblError").query();

        ((TextField) lookup("#tfNewServernameInput").query()).setText("TestServerName");

        clickOn("#btnSave");

        mockRestChangeServerName(serverChangeNameJson);

        // needed, so that we can assert that new name is set correctly, and lblError is also set correctly
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(lblError.getText(), "Error. Change Servername not successful!");
        Assert.assertTrue(lblError.isVisible());
        Assert.assertEquals(server.getName(), "AliceServer");
    }

    @Test
    public void showAttentionScreen() {
        localUser.setId("owner123");
        JsonObject serverInfoJson = buildServerInformationWithTwoMembers();
        JsonObject serverChangeNameJson = buildServerNameChangeFailure();
        mockRestExplicitServer(serverInfoJson);

        Assert.assertEquals(server.getName(), "AliceServer");

        clickOn("#btnEdit");

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", stageManager.getPopupStage().getTitle());

        clickOn("#btnDelete");

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(stageManager.getPopupStage().getTitle(), "Attention");
    }

    @Test
    public void createCountInvitationSuccessful() {
        localUser.setId("owner123");
        mockRestExplicitServer(buildServerInformationWithTwoMembers());
        clickOn("#btnEdit");
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(buildInvitationSuccessful().toString()));

        RadioButton radioBtnMaxCount = lookup("#radioBtnMaxCount").query();
        TextField tfMaxCountAmountInput = lookup("#tfMaxCountAmountInput").query();
        TextField tfInvitationLink = lookup("#tfInvitationLink").query();
        Label labelCopy = (Label) lookup("#labelCopy").query();

        clickOn(radioBtnMaxCount);
        Assert.assertTrue(tfMaxCountAmountInput.isEditable());
        tfMaxCountAmountInput.setText("15");
        clickOn("#btnCreateInvitation");
        WaitForAsyncUtils.waitForFxEvents();

        verify(restMock).createInvite(anyInt(), anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(buildInvitationSuccessful().getJsonObject(DATA).getString(LINK), tfInvitationLink.getText());
        Assert.assertEquals("Amount", tfMaxCountAmountInput.getPromptText());

        clickOn(tfInvitationLink);
        Assert.assertEquals("Copied", labelCopy.getText());
        WaitForAsyncUtils.asyncFx(() -> {
            Assert.assertEquals(Clipboard.getSystemClipboard().getString(), tfInvitationLink.getText());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void createTemporalInvitationSuccessful() {
        localUser.setId("owner123");
        mockRestExplicitServer(buildServerInformationWithTwoMembers());
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
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void createInvitationTestFailure() {
        localUser.setId("owner123");
        mockRestExplicitServer(buildServerInformationWithTwoMembers());
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

        Label labelCopy = (Label) lookup("#labelCopy").query();

        clickOn(tfInvitationLink);
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("First create invitation", labelCopy.getText());

        WaitForAsyncUtils.waitForFxEvents();
    }

    // helper methods that build jsonObjects in order to mock RestClient answers.

    private JsonObject buildServerNameChangeFailure() {
        return Json.createObjectBuilder().add("status", "failure").add("message", "Changing server name failed!")
                .add("data", Json.createObjectBuilder())
                .build();
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

    public JsonObject buildServerNameChangeSuccessful(String newServerName) {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", server.getId())
                        .add("name", newServerName))
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
