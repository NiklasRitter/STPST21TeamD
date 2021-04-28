package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginScreenController {

    private LocalUser localUser;
    private Editor editor;
    private Parent view;
    private Button btnLogin;
    private TextField tfUserName;
    private TextField pwUserPw;

    public LoginScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
    }

    public void init() {
        //Load all view references
        tfUserName = (TextField) view.lookup("#tfUserName");
        pwUserPw = (TextField) view.lookup("#pwUserPw");

        btnLogin = (Button) view.lookup("#btnLogin");

        btnLogin.setOnAction(this::loginButtonAction);

    }

    private void loginButtonAction(ActionEvent actionEvent) {
    }

    public void stop() {
    }

}
