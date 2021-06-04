package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OptionsScreenController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;

    private CheckBox btnDarkmode;
    private Button logoutButton;


    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public OptionsScreenController(Parent view, Options model, Editor editor) {
        this.view = view;
        this.options = model;
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
        this.btnDarkmode = (CheckBox) view.lookup("#btnDarkmode");
        this.logoutButton = (Button) view.lookup("#btnLogout");

        this.btnDarkmode.setSelected(options.isDarkmode());

        this.btnDarkmode.setOnAction(this::btnDarkmodeOnClick);
        this.logoutButton.setOnAction(this::logoutButtonOnClick);

        // If current stage is LoginScreen, than OptionScreen should not show logout button
        Stage stage = StageManager.getStage();
        if (stage.getTitle().equals("Login")) {
            logoutButton.setVisible(false);
            VBox parent = (VBox) logoutButton.getParent();
            parent.getChildren().remove(logoutButton);
            parent.setPrefHeight(80);
            parent.setPrefWidth(300);
        }
        Tooltip logoutButton = new Tooltip();
        logoutButton.setText("logout");
        this.logoutButton.setTooltip(logoutButton);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        btnDarkmode.setOnAction(null);
        logoutButton.setOnAction(null);
    }

    /**
     * Change the darkmode to the value of the CheckBox
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void btnDarkmodeOnClick(ActionEvent actionEvent) {
        options.setDarkmode(btnDarkmode.isSelected());
    }

    /**
     * The localUser will be logged out and redirect to the LoginScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void logoutButtonOnClick(ActionEvent actionEvent) {
        editor.logoutUser(editor.getLocalUser().getUserKey());
    }

}
