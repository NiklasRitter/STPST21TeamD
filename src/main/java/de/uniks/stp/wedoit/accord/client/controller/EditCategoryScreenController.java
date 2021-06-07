package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.List;

public class EditCategoryScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Category category;
    private TextField tfCategoryName;
    private Button btnEditCategory;
    private Button btnDeleteCategory;
    private Label errorLabel;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     * @param category The category to be changed
     */
    public EditCategoryScreenController(Parent view, LocalUser model, Editor editor, Category category) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.category = category;
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
        this.btnEditCategory = (Button) view.lookup("#btnEditCategory");
        this.btnDeleteCategory = (Button) view.lookup("#btnDeleteCategory");
        this.tfCategoryName = (TextField) view.lookup("#tfCategoryName");
        this.errorLabel = (Label) view.lookup("#lblError");

        // Add action listeners
        this.btnEditCategory.setOnAction(this::editCategoryButtonOnClick);
        this.btnDeleteCategory.setOnAction(this::deleteCategoryButtonOnClick);

        tfCategoryName.setText(category.getName());
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        // Remove all action listeners
        btnEditCategory.setOnAction(null);
        btnDeleteCategory.setOnAction(null);
    }


    /**
     * After pressing "Save", the category changes will be saved and you get
     * redirected to the Screen for the Server
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void editCategoryButtonOnClick(ActionEvent actionEvent) {
        if (tfCategoryName.getText().length() < 1 || tfCategoryName.getText() == null) {
            tfCategoryName.getStyleClass().add("error");

            Platform.runLater(() -> errorLabel.setText("Name has to be at least 1 symbols long"));
        } else {
            //editor.getNetworkController().updateCategory(editor.getCurrentServer(), category, tfCategoryName.getText(), this);
        }
    }

    public void handleEditCategory(Category category) {
        if (category != null) {
            Stage stage = (Stage) view.getScene().getWindow();
            Platform.runLater(stage::close);
            stop();
        } else {
            tfCategoryName.getStyleClass().add("error");
            Platform.runLater(() -> errorLabel.setText("Something went wrong while updating the category"));
        }
    }

    /**
     * After pressing "Delete", you get redirected to the deleteHandler
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void deleteCategoryButtonOnClick(ActionEvent actionEvent) {
        //StageManager.showAttentionScreen(category);
    }
}
