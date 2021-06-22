package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguagePreferences;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Locale;

public class OptionsScreenController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;

    private CheckBox btnDarkmode;
    private Button logoutButton, btnSave;
    private Label lblLanguage;
    private ChoiceBox choiseBoxLanguage;

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
        this.btnSave = (Button) view.lookup("#btnSave");
        this.lblLanguage = (Label) view.lookup("#lblLanguage");
        this.choiseBoxLanguage = (ChoiceBox) view.lookup("#choiseBoxLanguage");

        this.choiseBoxLanguage.getItems().add("English");
        this.choiseBoxLanguage.getItems().add("Deutsch");
        this.choiseBoxLanguage.getItems().add("فارسی");

        this.choiseBoxLanguage.setOnAction(this::choiseBoxLanguageOnClick);

        this.btnDarkmode.setSelected(options.isDarkmode());

        this.btnDarkmode.setOnAction(this::btnDarkmodeOnClick);
        this.logoutButton.setOnAction(this::logoutButtonOnClick);

        // If current stage is LoginScreen, than OptionScreen should not show logout button
        Stage stage = this.editor.getStageManager().getStage();
        if (stage.getTitle().equals("Login")) {
            logoutButton.setVisible(false);
            HBox parent = (HBox) logoutButton.getParent();
            parent.getChildren().remove(logoutButton);
        }
        stage.sizeToScene();
        Tooltip logoutButton = new Tooltip();
        logoutButton.setText("Logout");
        this.logoutButton.setTooltip(logoutButton);
    }

    private void choiseBoxLanguageOnClick(Event event) {
        Object selectedItem = this.choiseBoxLanguage.getSelectionModel().getSelectedItem();

        switch (selectedItem.toString()) {
            case "English":
                setLanguage("language/Language");
                break;
            case "Deutsch":
                setLanguage("language/Language_de_DE");
                break;
            case "فارسی":
                setLanguage("language/Language_fa_IR");
                break;
        }
        lblLanguage.setText(LanguageResolver.getString("LANGUAGE"));
    }

    private void setLanguage(String languageURL) {
        Locale.setDefault(LanguagePreferences.getLanguagePreferences().getCurrentLocale(languageURL));
        LanguageResolver.load();
        LanguagePreferences.getLanguagePreferences().setLanguage(languageURL);
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
