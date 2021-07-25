package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
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
        this.btnCancel = (Button) view.lookup("#btnCancel");
        this.btnSave = (Button) view.lookup("#btnSave");
        this.tfSteam64ID = (TextField) view.lookup("#tfSteam64ID");
        this.hlSteamIDLookup = (Hyperlink) view.lookup("#hlSteamIDLookup");

        this.editor.getStageManager().getStage(POPUP_STAGE).setTitle(LanguageResolver.getString("CONNECT_TO_STEAM"));


        this.btnCancel.setOnAction(this::btnCancelOnClick);
        this.btnSave.setOnAction(this::btnSaveOnClick);
        this.hlSteamIDLookup.setOnMouseClicked(this::hlSteamIDLookupOnClick);
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
        this.editor.getStageManager().initView(ControllerEnum.OPTION_SCREEN, null, null);
    }


    private void btnSaveOnClick(Object object) {
        localUser.setSteam64ID(tfSteam64ID.getText());
        editor.getRestManager().getLocalUserSteamGameExtraInfo();
        this.editor.getStageManager().initView(ControllerEnum.OPTION_SCREEN, null, null);
    }

    private void hlSteamIDLookupOnClick(Object object) {
        editor.getStageManager().getHostServices().showDocument(hlSteamIDLookup.getText());
    }

}
