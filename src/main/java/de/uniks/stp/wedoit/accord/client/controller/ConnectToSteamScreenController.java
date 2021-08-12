package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

import static de.uniks.stp.wedoit.accord.client.constants.StageEnum.POPUP_STAGE;

public class ConnectToSteamScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private Parent view;
    private Button btnCancel, btnSave;
    private TextField tfSteam64ID;
    private Hyperlink hlSteamIDLookup;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public ConnectToSteamScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
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
        btnCancel = (Button) view.lookup("#btnCancel");
        btnSave = (Button) view.lookup("#btnSave");
        tfSteam64ID = (TextField) view.lookup("#tfSteam64ID");
        hlSteamIDLookup = (Hyperlink) view.lookup("#hlSteamIDLookup");

        editor.getStageManager().getStage(POPUP_STAGE).setTitle(LanguageResolver.getString("CONNECT_TO_STEAM"));

        tfSteam64ID.setText(editor.getLocalUser().getSteam64ID());

        btnCancel.setOnAction(this::btnCancelOnClick);
        btnSave.setOnAction(this::btnSaveOnClick);
        hlSteamIDLookup.setOnMouseClicked(this::hlSteamIDLookupOnClick);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        btnCancel.setOnAction(null);
        btnCancel.setOnAction(null);
        hlSteamIDLookup.setOnAction(null);
    }


    private void btnCancelOnClick(Object object) {
//        Platform.runLater(() -> localUser.setSteam64ID(tfSteam64ID.getText()));
        editor.getStageManager().getStage(POPUP_STAGE).close();
    }


    private void btnSaveOnClick(Object object) {
        if (!tfSteam64ID.getText().isEmpty()) {
            localUser.setSteam64ID(tfSteam64ID.getText());
            editor.saveSteam64ID(tfSteam64ID.getText());
            editor.getRestManager().getLocalUserSteamGameExtraInfo();
            editor.getStageManager().getStage(POPUP_STAGE).close();
        }
    }

    private void hlSteamIDLookupOnClick(Object object) {
        editor.getStageManager().getHostServices().showDocument(hlSteamIDLookup.getText());
    }

}
