package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.language.LanguagePreferences;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;

import java.util.Locale;

public class LanguageController implements Controller {

    private Parent view;
    private final Options options;
    private final Editor editor;

    private ChoiceBox<String> choiceBoxLanguage;

    public LanguageController(Parent view, Options model, Editor editor){
        this.view = view;
        this.options = model;
        this.editor = editor;
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

        Scene scene = this.view.getScene();
        this.view = ControllerEnum.OPTION_SCREEN.loadScreen();
        ((HBox) view.lookup("#hBoxOuter")).getChildren().add( ControllerEnum.LANGUAGE_OPTIONS_SCREEN.loadSubOptionScreen());
        scene.setRoot(this.view);
        this.init();

    }

    private void setLanguage(String languageURL) {
        Locale.setDefault(LanguagePreferences.getLanguagePreferences().getCurrentLocale(languageURL));
        LanguageResolver.load();
        LanguagePreferences.getLanguagePreferences().setLanguage(languageURL);
    }
}
