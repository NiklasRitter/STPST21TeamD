package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.util.ResourceManager;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Objects;

public class OptionsScreenTest extends ApplicationTest {
    private RestClient restClient;
    private Stage stage;
    private Stage popupStage;
    private StageManager stageManager;
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
        this.oldOptions = ResourceManager.loadOptions();
        ResourceManager.saveOptions(new Options().setDarkmode(false));

        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.restClient = new RestClient();
        this.stageManager.start(stage);
        this.popupStage = this.stageManager.getPopupStage();

        this.stageManager.getEditor().getNetworkController().setRestClient(restClient);
        StageManager.showLoginScreen();
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Override
    public void stop() throws Exception {
        ResourceManager.saveOptions(this.oldOptions);
        super.stop();
        restClient = null;
        stage = null;
        popupStage = null;
        stageManager = null;
        oldOptions = null;
    }

    public void directToOptionsScreen() {
        clickOn("#btnOptions");
    }

    @Test
    public void testBtnDarkmode() {
        // open options screen
        directToOptionsScreen();

        // check if stylesheets contain light theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(popupStage.isShowing());
        Assert.assertEquals("Options", popupStage.getTitle());
        Assert.assertTrue(stageManager.getScene().getStylesheets()
                .contains(Objects.requireNonNull(StageManager.class.getResource("light-theme.css")).toExternalForm()));

        // test darkmode button
        clickOn("#btnDarkmode");

        // check if stylesheets contain dark theme
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertTrue(stageManager.getScene().getStylesheets()
                .contains(Objects.requireNonNull(StageManager.class.getResource("dark-theme.css")).toExternalForm()));

    }
}
