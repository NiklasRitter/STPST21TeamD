package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.testfx.framework.junit.ApplicationTest;

public class RegisterTest extends ApplicationTest{

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    private Stage stage;
    private StageManager app;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeEach()
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // FXML testen
    // Register Button on click testen
    // testen ob User da

    @Test()
    public void testRegister() {
        //TODO das ist kein sinnvoller Test
        restMock.register("test", "test123", response -> {});

        //successful answer
        when(res.getBody()).thenReturn(new JsonNode("{}"));
        verify(restMock).register(anyString(), anyString(), callbackCaptor.capture());

        Callback<JsonNode> callback = callbackCaptor.getValue();
        callback.completed(res);

        assertEquals("{}", res.getBody().toString());
    }

    @Test()
    public void testRegisterGUI () {
        clickOn("#tfUserName");
        write("test");

        clickOn("#pwUserPw");
        write("test123");

        clickOn("#btnRegister");

        assertEquals("Main" ,stage.getTitle());
    }
}
