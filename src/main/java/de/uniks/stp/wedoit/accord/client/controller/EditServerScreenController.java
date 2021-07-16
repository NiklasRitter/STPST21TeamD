package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Locale;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.ATTENTION_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.COUNT;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.TEMPORAL;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUPSTAGE;


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

    private Label labelCopy, lblChangeName, lblInvite, lblOldInvit, lblInvitationStatus, lblInvitationStatusText, lblCountWarning;

    private Label lblError;
    private ListView<Invitation> lvInvitation;
    private Button btnDeleteInvitation;
    private ObservableList<Invitation> invitationsObservableList;
    private PropertyChangeListener invitationsListener = this::invitationsChanged;


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
        this.btnDeleteInvitation = (Button) view.lookup("#btnDeleteInvitation");

        this.radioBtnTemporal = (RadioButton) view.lookup("#radioBtnTemporal");
        this.radioBtnMaxCount = (RadioButton) view.lookup("#radioBtnMaxCount");

        this.tfNewServernameInput = (TextField) view.lookup("#tfNewServernameInput");
        this.tfMaxCountAmountInput = (TextField) view.lookup("#tfMaxCountAmountInput");
        this.tfInvitationLink = (TextField) view.lookup("#tfInvitationLink");

        this.labelCopy = (Label) view.lookup("#labelCopy");
        this.lblError = (Label) view.lookup("#lblError");
        this.lblInvitationStatus = (Label) view.lookup("#lblInvitationStatus");
        this.lblInvitationStatusText = (Label) view.lookup("#lblInvitationStatusText");
        this.lblChangeName = (Label) view.lookup("#lblChangeName");
        this.lblInvite = (Label) view.lookup("#lblInvite");
        this.lblOldInvit = (Label) view.lookup("#lblOldInvit");
        this.lblCountWarning = (Label) view.lookup("#lblCountWarning");

        this.lvInvitation = (ListView<Invitation>) view.lookup("#lvInvitation");

        this.view.requestFocus();
        this.setComponentsText();
        // Depending on if localUser is admin or not display the correct editMenu
        loadDefaultSettings();

        // load old invitations and initialize lvInvitation
        loadOldInvitations();
        Tooltip privateChatsButton = new Tooltip();
        privateChatsButton.setText(LanguageResolver.getString("DOUBLE_CLICK_COPY_INVITATION"));
        this.lvInvitation.setTooltip(privateChatsButton);

        addActionListener();
    }

    private void setComponentsText() {
        this.lblError.setText(LanguageResolver.getString(LanguageResolver.getString("SOMETHING_WENT_WRONG")));
        this.lblChangeName.setText(LanguageResolver.getString("CHANGE_NAME"));
        this.lblInvite.setText(LanguageResolver.getString("INVITE"));
        this.lblOldInvit.setText(LanguageResolver.getString("OLD_INVITATIONS"));
        this.btnDeleteInvitation.setText(LanguageResolver.getString("DELETE_INVITATION"));
        this.btnDelete.setText(LanguageResolver.getString("DELETE_SERVER"));
        this.btnSave.setText(LanguageResolver.getString("SAVE"));
        this.btnCreateInvitation.setText(LanguageResolver.getString("CREATE_INVITATION"));
        this.tfMaxCountAmountInput.setPromptText(LanguageResolver.getString("AMOUNT"));
        this.tfNewServernameInput.setPromptText(LanguageResolver.getString("NEW_SERVERNAME"));
        this.radioBtnTemporal.setText(LanguageResolver.getString("TEMPORAL"));
        this.radioBtnMaxCount.setText(LanguageResolver.getString("MAX_COUNT"));
        this.editor.getStageManager().getPopupStage().sizeToScene();
        this.editor.getStageManager().getPopupStage().centerOnScreen();
    }

    /**
     * adds needed action listener for javafx nodes
     */
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
        this.server.listeners().removePropertyChangeListener(Server.PROPERTY_INVITATIONS, this.invitationsListener);
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
     * @param actionEvent expects an ActionEvent
     */
    private void saveButtonOnClick(ActionEvent actionEvent) {
        String newServerName = tfNewServernameInput.getText();
        if (!newServerName.isEmpty()) {
            editor.getRestManager().changeServerName(localUser, server, newServerName, this);
        } else {
            stage.close();
        }
    }

    /**
     * redirects to the attention screen to delete a server
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void deleteButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.ATTENTION_SCREEN, this.server, null);
    }

    /**
     * deletes a selected invitation and removes status text of the invitation
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void deleteInvitationButtonOnClick(ActionEvent actionEvent) {
        lblInvitationStatusText.setText("");
        lblInvitationStatus.setText("");
        if (lvInvitation.getSelectionModel().getSelectedItem() != null) {
            editor.getRestManager().deleteInvite(localUser.getUserKey(), lvInvitation.getSelectionModel().getSelectedItem(), server);
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
                lblCountWarning.setText(LanguageResolver.getString("INSERT_AMOUNT_>_0"));
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
        Platform.runLater(() -> resetAmountPromptText());
        if (invitationLink != null) {
            tfInvitationLink.setText(invitationLink);
        } else {
            Platform.runLater(() -> {
                tfInvitationLink.setPromptText(LanguageResolver.getString("GENERATION_FAILED"));
            });
        }
    }

    /**
     * sets the possibility to input a max amount of a invitation true
     */
    private void radioBtnMaxCountOnClick(MouseEvent mouseEvent) {
        resetAmountPromptText();
        if (this.radioBtnMaxCount.isFocused()) {
            this.tfMaxCountAmountInput.setEditable(true);
        }
    }

    /**
     * sets the possibility to input a max amount of a invitation false
     */
    private void radioBtnTemporalOnClick(MouseEvent mouseEvent) {
        resetAmountPromptText();

        if (this.radioBtnTemporal.isFocused()) {
            this.tfMaxCountAmountInput.setEditable(false);
        }
    }

    /**
     * resets amount input
     */
    private void resetAmountPromptText() {
        tfMaxCountAmountInput.setText("");
        tfMaxCountAmountInput.setPromptText(LanguageResolver.getString("AMOUNT"));
        tfMaxCountAmountInput.getStyleClass().removeAll("redPromptText");
        lblCountWarning.setText(null);
    }

    /**
     * This method copies the invitation link and put the link in the system clipboard
     * <p>
     * shows "Copied" for 2 seconds if there is a link
     * else shows "First create invitation"
     */
    private void copyInvitationLinkOnClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {

            if (!tfInvitationLink.getText().equals("")) {
                editor.copyToSystemClipBoard(tfInvitationLink.getText());
                labelCopy.setText(LanguageResolver.getString("COPIED"));
            } else {
                labelCopy.setText(LanguageResolver.getString("FIRST_CREATE_INVIT"));
            }

            resetLabelCopy();
        }
    }

    /**
     * This method copies the invitation link and put the link in the system clipboard
     * <p>
     * shows "Copied" for 2 seconds if there is a link
     * else shows "First create invitation"
     */
    private void copyLvInvitationLinkOnClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1 || mouseEvent.getClickCount() == 2) {

            if (lvInvitation.getSelectionModel().getSelectedItem() != null) {
                lblInvitationStatusText.setText(LanguageResolver.getString("INVITATION_STATUS"));
                Invitation invitation = lvInvitation.getSelectionModel().getSelectedItem();
                if (invitation.getType().equals(COUNT)) {
                    lblInvitationStatus.setText("usable " + (invitation.getMax() - invitation.getCurrent() + 1) + " more times");
                }
                if (invitation.getType().equals(TEMPORAL)) {
                    lblInvitationStatus.setText(LanguageResolver.getString("VALID_FOR_24"));
                }

            }

        }

        if (mouseEvent.getClickCount() == 2) {

            if (lvInvitation.getSelectionModel().getSelectedItem() != null) {
                editor.copyToSystemClipBoard(lvInvitation.getSelectionModel().getSelectedItem().getLink());

                labelCopy.setText(LanguageResolver.getString("COPIED"));

            } else {
                labelCopy.setText(LanguageResolver.getString("SELECT_INVITATION"));
            }

            resetLabelCopy();
        }
    }

    /**
     * resets the labelCopy
     */
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

    /**
     * handles the updating of a server.
     *
     * @param status status which is true if updating was successful
     */
    public void handleChangeServerName(boolean status) {
        if (status) {
            Platform.runLater(this.stage::close);
        } else {
            Platform.runLater(() -> {
                lblError.setText(LanguageResolver.getString("ERROR_CHANGE_SERVERNAME"));
                lblError.setVisible(true);
            });
        }
    }

    /**
     * loads invitation for a certain server
     */
    private void loadOldInvitations() {
        this.editor.getRestManager().loadInvitations(server, localUser.getUserKey(), this);
    }

    /**
     * handle the loading of invitations and adds the new invitations to the invitations list.
     *
     * @param invitations invitations of a server
     */
    public void handleOldInvitations(List<Invitation> invitations) {
        if (invitations != null) {
            createLvInvitations();
        } else {
            Platform.runLater(() -> {
                lblError.setText(LanguageResolver.getString("ERROR_WHILE_LOADING_INVIT"));
                lblError.setVisible(true);
            });

        }
    }

    /**
     * create a list view filled with invitations, but only links are shown
     */
    private void createLvInvitations() {
        this.invitationsObservableList = FXCollections.observableList(server.getInvitations());

        lvInvitation.setCellFactory(new InvitationListView());
        Platform.runLater(() -> lvInvitation.setItems(invitationsObservableList));

        server.listeners().addPropertyChangeListener(Server.PROPERTY_INVITATIONS, this.invitationsListener);
    }

    /**
     * updates a invitations list
     *
     * @param propertyChangeEvent event which is fired if the invitations of server have changed
     */
    private void invitationsChanged(PropertyChangeEvent propertyChangeEvent) {
        this.invitationsObservableList = FXCollections.observableList(server.getInvitations());
        Platform.runLater(() -> lvInvitation.setItems(invitationsObservableList));
    }


}
