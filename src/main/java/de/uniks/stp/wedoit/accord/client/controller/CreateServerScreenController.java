package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.lang.invoke.MethodHandles;

public class CreateServerScreenController {

    private LocalUser localUser;
    private Editor editor;
    private Parent view;
    private TextField tfServerName;
    private Button btnCreateServer;

    public CreateServerScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
    }

    public void init(){
        // Load all view references
        this.btnCreateServer = (Button) view.lookup("#btnCreateServer");
        this.tfServerName = (TextField) view.lookup("tfServerName");

        // Add action listeners
        this.btnCreateServer.setOnAction(this::createServerButtonOnClick);
    }

    public void stop(){
        btnCreateServer.setOnAction(null);
    }

    // Additional methods

    /**
     * After pressing "Create Server", the server will be created with the name in the textfield, and you get redirected
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private String createServerButtonOnClick(ActionEvent actionEvent) {
        return tfServerName.getText();
    }
}
