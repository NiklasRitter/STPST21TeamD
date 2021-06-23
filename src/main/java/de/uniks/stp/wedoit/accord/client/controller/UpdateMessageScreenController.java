package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;

public class UpdateMessageScreenController implements Controller{

    private final Parent view;
    private final Editor editor;
    private final Message message;
    private TextField tfUpdateMessage;
    private Button btnDiscard;
    private Button btnUpdateMessage;
    private Label errorLabel;

    public UpdateMessageScreenController(Parent view, Editor editor, Message message) {
        this.view = view;
        this.editor = editor;
        this.message = message;
    }

    @Override
    public void init() {
        tfUpdateMessage = (TextField) view.lookup("#tfUpdateMessage");
        btnDiscard = (Button) view.lookup("#btnDiscard");
        btnUpdateMessage = (Button) view.lookup("#btnUpdateMessage");
        errorLabel = (Label) view.lookup("#lblError");
        
        tfUpdateMessage.setText(message.getText());
        
        btnDiscard.setOnAction(this::discardChanges);
        btnUpdateMessage.setOnAction(this::updateMessage);
        
    }

    private void updateMessage(ActionEvent actionEvent) {
        String newMessage = tfUpdateMessage.getText();

        if (newMessage.equals(message.getText())) {
            this.editor.getStageManager().getPopupStage().close();
        }
        else if (newMessage.length() >= 1) {
            editor.getRestManager().updateMessage(message, newMessage);
        }
        else {
            Platform.runLater(() -> errorLabel.setText("Updated message needs at least 1 character!"));
        }
    }

    private void discardChanges(ActionEvent actionEvent) {
        this.editor.getStageManager().getPopupStage().close();
    }

    @Override
    public void stop() {
        btnDiscard.setOnAction(null);
        btnUpdateMessage.setOnAction(null);

        tfUpdateMessage = null;
        btnDiscard = null;
        btnUpdateMessage = null;
        errorLabel = null;
    }
    
}
