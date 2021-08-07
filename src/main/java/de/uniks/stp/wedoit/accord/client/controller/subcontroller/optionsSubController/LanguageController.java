package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.OptionsScreenController;
import de.uniks.stp.wedoit.accord.client.language.LanguagePreferences;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Locale;

public class LanguageController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;
    private final OptionsScreenController controller;

    private ChoiceBox<String> choiceBoxLanguage;

    public LanguageController(Parent view, Options model, Editor editor, OptionsScreenController controller){
        this.view = view;
        this.options = model;
        this.editor = editor;
        this.controller = controller;
    }

    @Override
    public void init() {
        this.choiceBoxLanguage = (ChoiceBox<String>) view.lookup("#choiceBoxLanguage");

        createChoiceBoxItems();
    }

    @Override
    public void stop() {
        this.choiceBoxLanguage.setOnAction(null);

    }

    private void createChoiceBoxItems() {
        this.choiceBoxLanguage.getItems().addAll("English", "Deutsch", "فارسی");

        switch (Locale.getDefault().getLanguage()) {
            case "fa_ir":
                this.choiceBoxLanguage.getSelectionModel().select(2);
                break;
            case "de_de":
                this.choiceBoxLanguage.getSelectionModel().select(1);
                break;
            case "en_gb":
                this.choiceBoxLanguage.getSelectionModel().select(0);
                break;
        }
        this.choiceBoxLanguage.setOnAction(this::choiceBoxLanguageOnClick);
    }

    private void choiceBoxLanguageOnClick(Event event) {
        Object selectedItem = this.choiceBoxLanguage.getSelectionModel().getSelectedItem();

        switch (selectedItem.toString()) {
            case "English":
                setLanguage("language/Language");
                options.setLanguage("en_GB");
                break;
            case "Deutsch":
                setLanguage("language/Language_de_DE");
                options.setLanguage("de_DE");
                break;
            case "فارسی":
                setLanguage("language/Language_fa_IR");
                options.setLanguage("fa_IR");
                break;
        }
        controller.selectSubController(ControllerEnum.LANGUAGE_OPTIONS_SCREEN);
    }

    private void setLanguage(String languageURL) {
        Locale.setDefault(LanguagePreferences.getLanguagePreferences().getCurrentLocale(languageURL));
        LanguageResolver.load();
        LanguagePreferences.getLanguagePreferences().setLanguage(languageURL);
    }
}
