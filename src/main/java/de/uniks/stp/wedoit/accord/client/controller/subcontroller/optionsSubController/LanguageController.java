package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.Icons;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.OptionsScreenController;
import de.uniks.stp.wedoit.accord.client.language.LanguagePreferences;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.richtext.RichTextArea;
import de.uniks.stp.wedoit.accord.client.view.EmojiButton;
import javafx.beans.Observable;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.codec.language.bm.Lang;

import javax.swing.text.DefaultEditorKit;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LanguageController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;
    private final OptionsScreenController controller;

    private VBox vBoxLanguage;
    private final ToggleGroup toggleGroup = new ToggleGroup();


    public LanguageController(Parent view, Options model, Editor editor, OptionsScreenController controller){
        this.view = view;
        this.options = model;
        this.editor = editor;
        this.controller = controller;
    }

    @Override
    public void init() {
        String currentLanguage = editor.getStageManager().getPrefManager().loadLanguage();
        this.vBoxLanguage = (VBox) view.lookup("#vBoxLanguage");

        //createChoiceBoxItems();
        toggleGroup.selectedToggleProperty().addListener(this::radioButtonOnClick);

        for(String lang: List.of("English", "Deutsch","فارسی")){

            HBox hBoxLanguageItem = new HBox();
            hBoxLanguageItem.getStyleClass().add("LanguageBox");

            RadioButton radioButton = new RadioButton(lang);
            radioButton.setPadding(new Insets(5,0,0,0));
            radioButton.setAlignment(Pos.CENTER);
            radioButton.setToggleGroup(toggleGroup);

            EmojiTextFlow etf = new EmojiTextFlow();
            switch (lang){
                case "Deutsch":
                    etf.parseAndAppend(Icons.GERMANY_FLAG.toString());
                    if(currentLanguage.equals("de_DE")){
                        radioButton.setSelected(true);
                    }
                    break;
                case "English":
                    etf.parseAndAppend(Icons.ENGLISH_FLAG.toString());
                    if(currentLanguage.equals("en_GB")){
                        radioButton.setSelected(true);
                    }
                    break;
                case "فارسی":
                    etf.parseAndAppend(Icons.PERSIAN_FLAG.toString());
                    if(currentLanguage.equals("fa_IR")){
                        radioButton.setSelected(true);
                    }
                    break;
            }

            hBoxLanguageItem.getChildren().addAll(radioButton,etf);

            vBoxLanguage.getChildren().add(hBoxLanguageItem);
        }
    }

    @Override
    public void stop() {
        toggleGroup.selectedToggleProperty().removeListener(this::radioButtonOnClick);
    }

    private void radioButtonOnClick(Observable event, Toggle oldToggle, Toggle newToggle) {

        RadioButton selectedItem = (RadioButton) newToggle;
        RadioButton oldItem = (RadioButton) oldToggle;
        if(oldItem != null) oldItem.setSelected(false);

        selectedItem.setSelected(true);


        switch (selectedItem.getText()) {
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
        if(oldItem != null) controller.selectSubController(ControllerEnum.LANGUAGE_OPTIONS_SCREEN);
    }

    private void setLanguage(String languageURL) {
        Locale.setDefault(LanguagePreferences.getLanguagePreferences().getCurrentLocale(languageURL));
        LanguageResolver.load();
        LanguagePreferences.getLanguagePreferences().setLanguage(languageURL);
    }
}
