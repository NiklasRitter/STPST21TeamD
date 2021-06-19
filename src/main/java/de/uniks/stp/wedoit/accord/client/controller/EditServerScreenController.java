package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Invitation;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.view.InvitationListView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.COUNT;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.TEMPORAL;


public class EditServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;
    private final Stage stage;

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

    private Label lblError;
    private ListView<Invitation> lvInvitation;
    private Button btnDeleteInvitation;
    private ObservableList<Invitation> invitationsObservableList;
    private PropertyChangeListener invitationsListener = this::invitationsChanged;
    private Label lblInvitationStatus;
    private Label lblInvitationStatusText;


    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     * @param server The Server this Screen belongs to
     */
    public EditServerScreenController(Parent view, LocalUser model, Editor editor, Server server, Stage stage) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.server = server;
        this.stage = stage;
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
        this.lblError = (Label) view.lookup("#lblError");
        this.lblInvitationStatus = (Label) view.lookup("#lblInvitationStatus");
        this.lblInvitationStatusText = (Label) view.lookup("#lblInvitationStatusText");


        this.lvInvitation = (ListView<Invitation>) view.lookup("#lvInvitation");
        this.btnDeleteInvitation = (Button) view.lookup("#btnDeleteInvitation");

        // Depending on if localUser is admin or not display the correct editMenu
        loadDefaultSettings();

        // load old invitations and initialize lvInvitation
        loadOldInvitations();
        Tooltip privateChatsButton = new Tooltip();
        privateChatsButton.setText("double click to copy invitation");
        this.lvInvitation.setTooltip(privateChatsButton);

        addActionListener();
    }

    private void addActionListener() {
        // Add action listeners
        this.btnCreateInvitation.setOnAction(this::createInvitationButtonOnClick);
        this.btnDelete.setOnAction(this::deleteButtonOnClick);
        this.btnSave.setOnAction(this::saveButtonOnClick);
        this.btnDeleteInvitation.setOnAction(this::deleteInvitationButtonOnClick);
        this.radioBtnMaxCount.setOnMouseClicked(this::radioBtnMaxCountOnClick);
        this.radioBtnTemporal.setOnMouseClicked(this::radioBtnTemporalOnClick);
        this.tfInvitationLink.setOnMouseClicked(this::copyInvitationLinkOnClick);
        this.lvInvitation.setOnMouseClicked(this::copyLvInvitationLinkOnClick);

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
        this.tfInvitationLink.setOnMouseClicked(null);
        this.lvInvitation.setOnMouseClicked(null);
        this.btnDeleteInvitation.setOnAction(null);
        server.listeners().removePropertyChangeListener(Server.PROPERTY_INVITATIONS, this.invitationsListener);
        this.invitationsListener = null;

    }

    /**
     * Called to load the correct EditorScreen depending on whether the localUser is admin of server or not
     */
    private void loadDefaultSettings() {

        lblError.setVisible(false);
        ToggleGroup toggleGroup = new ToggleGroup();
        radioBtnMaxCount.setToggleGroup(toggleGroup);
        radioBtnTemporal.setToggleGroup(toggleGroup);
        radioBtnMaxCount.setSelected(true);

    }

    /**
     * In this method a new servername has to be set if set if the
     * user types in a new servername and close popup Window
     *
     * @param actionEvent
     */
    private void saveButtonOnClick(ActionEvent actionEvent) {
        String newServerName = tfNewServernameInput.getText();
        if (!newServerName.isEmpty()) {
            editor.getRestManager().changeServerName(localUser, server, newServerName, this);
        } else {
            stage.close();
        }
    }

    private void deleteButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().showAttentionScreen(this.server);
    }


    private void deleteInvitationButtonOnClick(ActionEvent actionEvent) {
        lblInvitationStatusText.setText("");
        lblInvitationStatus.setText("");
        if (lvInvitation.getSelectionModel().getSelectedItem() != null) {
            editor.getRestManager().deleteInvite(localUser.getUserKey(), lvInvitation.getSelectionModel().getSelectedItem(), server, this);
        }
    }


    /**
     * Call the network controller if the input for a invitation is valid
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void createInvitationButtonOnClick(ActionEvent actionEvent) {
        if (radioBtnMaxCount.isSelected()) {
            if (tfMaxCountAmountInput.getText().matches("[1-9][0-9]*") && tfMaxCountAmountInput.getText().length() < 10) {
                int max = Integer.parseInt(tfMaxCountAmountInput.getText());
                editor.getRestManager().createInvitation(COUNT, max, server, localUser.getUserKey(), this);
            } else {
                tfMaxCountAmountInput.setText("");
                tfMaxCountAmountInput.setPromptText("Insert Amount > 0");
                tfMaxCountAmountInput.getStyleClass().add("redPromptText");
            }
        } else if (radioBtnTemporal.isSelected()) {
            editor.getRestManager().createInvitation(TEMPORAL, 0, server, localUser.getUserKey(), this);
        }
    }

    /**
     * handle a new invitation link in the EditServerScreen and show the link in the screen
     *
     * @param invitationLink invitation link which is responded by the server
     */
    public void handleInvitation(String invitationLink) {
        resetAmountPromptText();
        if (invitationLink != null) {
            tfInvitationLink.setText(invitationLink);
        } else {
            tfInvitationLink.setPromptText("generation failed");
        }
    }

    private void radioBtnMaxCountOnClick(MouseEvent mouseEvent) {
        resetAmountPromptText();
        if (this.radioBtnMaxCount.isFocused()) {
            this.tfMaxCountAmountInput.setEditable(true);
        }
    }

    private void radioBtnTemporalOnClick(MouseEvent mouseEvent) {
        resetAmountPromptText();

        if (this.radioBtnTemporal.isFocused()) {
            this.tfMaxCountAmountInput.setEditable(false);
        }
    }
    private void resetAmountPromptText() {
        tfMaxCountAmountInput.setText("");
        tfMaxCountAmountInput.setPromptText("Amount");
        tfMaxCountAmountInput.getStyleClass().removeAll("redPromptText");
    }

    /**
     * This method copies the invitation link and put the link in the system clipboard
     * <p>
     * shows "Copied" for 1.5 seconds if there is a link
     * else shows "First create invitation"
     */
    private void copyInvitationLinkOnClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {

            if (!tfInvitationLink.getText().equals("")) {
                editor.copyToSystemClipBoard(tfInvitationLink.getText());

                labelCopy.setText("Copied");

            } else {
                labelCopy.setText("First create invitation");
            }

            resetLabelCopy();
        }
    }

    /**
     * This method copies the invitation link and put the link in the system clipboard
     * <p>
     * shows "Copied" for 1.5 seconds if there is a link
     * else shows "First create invitation"
     */
    private void copyLvInvitationLinkOnClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1 || mouseEvent.getClickCount() == 2) {

            if (lvInvitation.getSelectionModel().getSelectedItem() != null) {
                lblInvitationStatusText.setText("invitation status:");
                Invitation invitation = lvInvitation.getSelectionModel().getSelectedItem();
                if (invitation.getType().equals(COUNT)) {
                    lblInvitationStatus.setText("usable " + (invitation.getMax() - invitation.getCurrent() + 1) + " more times");
                }
                if (invitation.getType().equals(TEMPORAL)) {
                    lblInvitationStatus.setText("valid for less than 24 hours");
                }

            }

        }

        if (mouseEvent.getClickCount() == 2) {

            if (lvInvitation.getSelectionModel().getSelectedItem() != null) {
                editor.copyToSystemClipBoard(lvInvitation.getSelectionModel().getSelectedItem().getLink());

                labelCopy.setText("Copied");

            } else {
                labelCopy.setText("Select invitation");
            }

            resetLabelCopy();
        }
    }

    private void resetLabelCopy() {
        PauseTransition visiblePause = new PauseTransition(
                Duration.seconds(2)
        );
        visiblePause.setOnFinished(
                event -> {
                    if (((Stage) view.getScene().getWindow()).getTitle().equals("Edit Server")) {
                        if (!labelCopy.getText().equals("")) {
                            Platform.runLater(() -> labelCopy.setText(""));
                        }
                    }
                });
        visiblePause.play();
    }


    public void handleChangeServerName(boolean status) {
        if (status) {
            Platform.runLater(() -> {
                this.stage.close();
            });
        } else {
            Platform.runLater(() -> {
                lblError.setText("Error. Change Servername not successful!");
                lblError.setVisible(true);
            });
        }
    }

    private void loadOldInvitations() {
        this.editor.getRestManager().loadInvitations(server, localUser.getUserKey(), this);
    }

    public void handleOldInvitations(List<Invitation> invitations) {
        if (invitations != null) {
            createLvInvitations(invitations);
        } else {
            Platform.runLater(() -> {
                lblError.setText("Error while loading invitations");
                lblError.setVisible(true);
            });

        }
    }

    private void createLvInvitations(List<Invitation> invitations) {
        this.invitationsObservableList = FXCollections.observableList(server.getInvitations());

        lvInvitation.setCellFactory(new InvitationListView());
        Platform.runLater(() -> lvInvitation.setItems(invitationsObservableList));

        server.listeners().addPropertyChangeListener(Server.PROPERTY_INVITATIONS, this.invitationsListener);
    }

    private void invitationsChanged(PropertyChangeEvent propertyChangeEvent) {
        this.invitationsObservableList = FXCollections.observableList(server.getInvitations());
        Platform.runLater(() -> lvInvitation.setItems(invitationsObservableList));
    }


}
