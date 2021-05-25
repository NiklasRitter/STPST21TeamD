package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

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

        this.vBoxAdminOnly = (VBox) view.lookup("#vBoxAdminOnly");

        // TODO: Depending on if localUser is admin or not display the correct editMenu
        // TODO: implement function of buttons and so on
        

        addActionListener();
    }

    private void addActionListener() {
        // Add action listeners
        this.btnCreateInvitation.setOnAction(this::createInvitationButtonOnClick);
        this.btnDelete.setOnAction(this::deleteButtonOnClick);
        this.btnSave.setOnAction(this::saveButtonOnClick);
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
    }

    private void saveButtonOnClick(ActionEvent actionEvent) {

    }

    private void deleteButtonOnClick(ActionEvent actionEvent) {

    }

    private void createInvitationButtonOnClick(ActionEvent actionEvent) {

    }
}
