package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguagePreferences;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Locale;

public class OptionsScreenController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;

    private CheckBox btnDarkMode;
    private Button logoutButton;
    private Label lblLanguage, lblDarkMode;
    private ChoiceBox choiceBoxLanguage;

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
        this.btnDarkMode = (CheckBox) view.lookup("#btnDarkMode");
        this.logoutButton = (Button) view.lookup("#btnLogout");
        this.lblLanguage = (Label) view.lookup("#lblLanguage");
        this.lblDarkMode = (Label) view.lookup("#lblDarkMode");

        setComponentsText();

        this.choiceBoxLanguage = (ChoiceBox) view.lookup("#choiceBoxLanguage");
        createChoiceBoxItems();


        this.btnDarkMode.setSelected(options.isDarkmode());

        this.btnDarkMode.setOnAction(this::btnDarkmodeOnClick);
        this.logoutButton.setOnAction(this::logoutButtonOnClick);

        // If current stage is LoginScreen, than OptionScreen should not show logout button
        Stage stage = this.editor.getStageManager().getStage();
        if (stage.getTitle().equals("Login")) {
            logoutButton.setVisible(false);
            HBox parent = (HBox) logoutButton.getParent();
            parent.getChildren().remove(logoutButton);
        }

        Tooltip logoutButton = new Tooltip();
        logoutButton.setText(LanguageResolver.getString("LOGOUT"));
        this.logoutButton.setTooltip(logoutButton);
    }

    private void createChoiceBoxItems() {
        this.choiceBoxLanguage.getItems().add("English");
        this.choiceBoxLanguage.getItems().add("Deutsch");
        this.choiceBoxLanguage.getItems().add("فارسی");

        if (Locale.getDefault().getLanguage().equals("fa_ir")) {
            this.choiceBoxLanguage.getSelectionModel().select(2);
        } else if (Locale.getDefault().getLanguage().equals("de_de")) {
            this.choiceBoxLanguage.getSelectionModel().select(1);
        } else if (Locale.getDefault().getLanguage().equals("en_gb")) {
            this.choiceBoxLanguage.getSelectionModel().select(0);
        }
        this.choiceBoxLanguage.setOnAction(this::choiceBoxLanguageOnClick);
    }

    private void setComponentsText() {
        this.lblDarkMode.setText(LanguageResolver.getString("DARKMODE"));
        this.lblLanguage.setText(LanguageResolver.getString("LANGUAGE"));
        this.editor.getStageManager().getPopupStage().setTitle(LanguageResolver.getString("OPTIONS"));
    }

    private void choiceBoxLanguageOnClick(Event event) {
        Object selectedItem = this.choiceBoxLanguage.getSelectionModel().getSelectedItem();

        switch (selectedItem.toString()) {
            case "English":
                options.setLanguage("en_GB");
                setLanguage("language/Language");
                break;
            case "Deutsch":
                options.setLanguage("de_DE");
                setLanguage("language/Language_de_DE");
                break;
            case "فارسی":
                options.setLanguage("fa_IR");
                setLanguage("language/Language_fa_IR");
                break;
        }
        setComponentsText();
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
        btnDarkMode.setOnAction(null);
        logoutButton.setOnAction(null);
    }

    /**
     * Change the darkmode to the value of the CheckBox
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void btnDarkmodeOnClick(ActionEvent actionEvent) {
        options.setDarkmode(btnDarkMode.isSelected());
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
