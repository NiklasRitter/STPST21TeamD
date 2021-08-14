package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.network.spotify.SpotifyIntegration;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class ConnectionsController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;

    private Button btnSpotify, btnSteam;

    public ConnectionsController(Parent view, Options model, Editor editor){
        this.view = view;
        this.options = model;
        this.editor = editor;
    }


    @Override
    public void init() {
        this.btnSpotify = (Button) view.lookup("#btnSpotify");
        this.btnSteam = (Button) view.lookup("#btnSteam");

        this.btnSteam.setOnAction(this::btnSteamOnClick);
        this.btnSpotify.setOnAction(this::btnSpotifyClick);
    }

    @Override
    public void stop() {
        btnSteam.setOnAction(null);
        btnSpotify.setOnAction(null);
    }

    private void btnSteamOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.CONNECT_TO_STEAM_SCREEN, null, null);
    }

    private void btnSpotifyClick(ActionEvent actionEvent) {
        this.editor.setSpotifyIntegration(new SpotifyIntegration(editor));
        this.editor.getSpotifyIntegration().authorize();
    }
}
