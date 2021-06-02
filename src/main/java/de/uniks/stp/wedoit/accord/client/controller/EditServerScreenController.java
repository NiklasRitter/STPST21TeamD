package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.COUNT;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.TEMPORAL;

public class EditServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;

    private Button btnCreateInvitation;
    private Button btnDelete;
    private Button btnSave;

    private RadioButton radioBtnTemporal;
    private RadioButton radioBtnMaxCount;

    private TextField tfNewServernameInput;
    private TextField tfMaxCountAmountInput;
    private TextField tfInvitationLink;

    private VBox vBoxAdminOnly;
    private VBox mainVBox;
    private Label labelCopy;


    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     * @param server The Server this Screen belongs to
     */
    public EditServerScreenController(Parent view, LocalUser model, Editor editor, Server server) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.server = server;
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     */
    public void init() {
        // Load all view references
        this.btnCreateInvitation = (Button) view.lookup("#btnCreateInvitation");
        this.btnDelete = (Button) view.lookup("#btnDelete");
        this.btnSave = (Button) view.lookup("#btnSave");

        this.radioBtnTemporal = (RadioButton) view.lookup("#radioBtnTemporal");
        this.radioBtnMaxCount = (RadioButton) view.lookup("#radioBtnMaxCount");

        this.tfNewServernameInput = (TextField) view.lookup("#tfNewServernameInput");
        this.tfMaxCountAmountInput = (TextField) view.lookup("#tfMaxCountAmountInput");
        this.tfInvitationLink = (TextField) view.lookup("#tfInvitationLink");
        this.labelCopy = (Label) view.lookup("#labelCopy");

        this.vBoxAdminOnly = (VBox) view.lookup("#vBoxAdminOnly");
        this.mainVBox = (VBox) view.lookup("#mainVBox");

        // Depending on if localUser is admin or not display the correct editMenu
        loadDefaultSettings();

        // TODO: implement function of buttons and so on


        addActionListener();
    }

    private void addActionListener() {
        // Add action listeners
        this.btnCreateInvitation.setOnAction(this::createInvitationButtonOnClick);
        this.btnDelete.setOnAction(this::deleteButtonOnClick);
        this.btnSave.setOnAction(this::saveButtonOnClick);
        this.radioBtnMaxCount.setOnMouseClicked(this::radioBtnMaxCountOnClick);
        this.radioBtnTemporal.setOnMouseClicked(this::radioBtnTemporalOnClick);
        this.tfInvitationLink.setOnMouseClicked(this::copyInvitationLinkOnClick);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        this.btnCreateInvitation.setOnAction(null);
        this.btnDelete.setOnAction(null);
        this.btnSave.setOnAction(null);
        this.radioBtnMaxCount.setOnMouseClicked(null);
        this.radioBtnTemporal.setOnMouseClicked(null);
    }

    /**
     * Called to load the correct EditorScreen depending on whether the localUser is admin of server or not
     */
    private void loadDefaultSettings() {
        if (!localUser.getId().equals(server.getOwner())) {
            this.btnDelete.setVisible(false);
            this.btnDelete.setDisable(true);
            vBoxAdminOnly.setVisible(false);
            vBoxAdminOnly.setDisable(true);
            for (Node child : this.mainVBox.getChildren()) {
                child.getId();
                if (child.getId() != null && child.getId().equals("vBoxAdminOnly")) {
                    this.mainVBox.getChildren().remove(child);
                    break;
                }
            }
            this.mainVBox.setPrefHeight(150);
            this.mainVBox.setPrefWidth(350);
        } else {
            ToggleGroup invitationToggleGroup = new ToggleGroup();
            radioBtnMaxCount.setToggleGroup(invitationToggleGroup);
            radioBtnTemporal.setToggleGroup(invitationToggleGroup);
            radioBtnMaxCount.setSelected(true);
        }
    }

    private void saveButtonOnClick(ActionEvent actionEvent) {

    }

    private void deleteButtonOnClick(ActionEvent actionEvent) {

    }

    /**
     * Call the network controller if the input for a invitation is valid
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void createInvitationButtonOnClick(ActionEvent actionEvent) {
        if (radioBtnMaxCount.isSelected()) {
            if (tfMaxCountAmountInput.getText().matches("[1-9][0-9]*")) {
                int max = Integer.parseInt(tfMaxCountAmountInput.getText());
                editor.getNetworkController().createInvitation(COUNT, max, server.getId(), localUser.getUserKey(), this);
            } else {
                tfMaxCountAmountInput.setText("");
                tfMaxCountAmountInput.setPromptText("Insert Amount > 0");
            }
        } else if (radioBtnTemporal.isSelected()) {
            editor.getNetworkController().createInvitation(TEMPORAL, 0, server.getId(), localUser.getUserKey(), this);
        }
    }

    /**
     * handle a new invitation link in the EditServerScreen and show the link in the screen
     * @param invitationLink invitation link which is responded by the server
     */
    public void handleInvitation(String invitationLink) {
        tfMaxCountAmountInput.setText("");
        tfMaxCountAmountInput.setPromptText("Amount");
        if (invitationLink != null) {
            tfInvitationLink.setText(invitationLink);
        } else {
            tfInvitationLink.setPromptText("generation failed");
        }
    }

    private void radioBtnMaxCountOnClick(MouseEvent mouseEvent) {
        if (this.radioBtnMaxCount.isFocused()) {
            this.tfMaxCountAmountInput.setEditable(true);
        }
    }

    private void radioBtnTemporalOnClick(MouseEvent mouseEvent) {
        if (this.radioBtnTemporal.isFocused()) {
            this.tfMaxCountAmountInput.setEditable(false);
        }
    }

    /**
     * This method copies the invitation link and put the link in the system clipboard
     * <p>
     * shows "Copied" for 1.5 seconds if there is a link
     * else shows "First create invitation"
     *
     */
    private void copyInvitationLinkOnClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {

            if (!tfInvitationLink.getText().equals("")) {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(tfInvitationLink.getText());
                clipboard.setContent(content);
                labelCopy.setText("Copied");

            } else {
                labelCopy.setText("First create invitation");
            }
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    if (((Stage) view.getScene().getWindow()).getTitle().equals("Edit Server")) {
                        labelCopy.setText("");
                    }
                });
            }).start();
        }
    }
}
