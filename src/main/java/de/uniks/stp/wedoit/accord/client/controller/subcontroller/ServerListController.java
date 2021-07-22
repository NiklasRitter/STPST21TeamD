package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.AND_SERVER_ID_URL;

public class ServerListController implements Controller {

    private final Parent view;
    private final AccordClient model;
    private final Editor editor;
    private Button btnOptions;
    private Button btnHome;
    private Button addServerButton;
    private Button enterInvitationButton;
    private ListView lvServerList;

    ServerListController(Parent view, AccordClient model, Editor editor) {
        this.view = view;
        this.model = model;
        this.editor = editor;
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     */
    public void init() {
        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.addServerButton = (Button) view.lookup("#btnAddServer");
        this.enterInvitationButton = (Button) view.lookup("#btnEnterInvitation");
        this.lvServerList = (ListView) view.lookup("#lvServerList");

        this.btnOptions.setOnAction(this::optionsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
        this.addServerButton.setOnAction(this::addServerButtonOnClick);
        this.enterInvitationButton.setOnAction(this::enterInvitationButtonOnClick);
    }

    public void stop() {
        this.btnOptions.setOnAction(null);
        this.btnHome.setOnAction(null);
        this.addServerButton.setOnAction(null);
        this.enterInvitationButton.setOnAction(null);
    }

    /**
     * The localUser will be redirect to the HomeScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void homeButtonOnClick(ActionEvent actionEvent) {
        if ()
        this.editor.getStageManager().initView(ControllerEnum.MAIN_SCREEN, null, null);
    }

    /**
     * The localUser will be redirect to the OptionsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void optionsButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.OPTION_SCREEN, null, null);
    }

    /**
     * Opens a pop-up windows, where you can enter the servername
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void addServerButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.CREATE_SERVER_SCREEN, null, null);
    }

    /**
     * Opens a pop-up windows, where you can enter a invitation
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void enterInvitationButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.JOIN_SERVER_SCREEN, null, null);
    }
}
