package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Category;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditCategoryScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final Category category;
    private TextField tfCategoryName;
    private Button btnEditCategory;
    private Button btnDeleteCategory;
    private Label errorLabel, lblCategoryName;

    /**
     * Create a new Controller
     *
     * @param view     The view this Controller belongs to
     * @param editor   The editor of the Application
     * @param category The category to be changed
     */
    public EditCategoryScreenController(Parent view, Editor editor, Category category) {
        this.view = view;
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
        this.lblCategoryName = (Label) view.lookup("#lblCategoryName");

        this.view.requestFocus();
        this.setComponentsText();

        // Add action listeners
        this.btnEditCategory.setOnAction(this::editCategoryButtonOnClick);
        this.btnDeleteCategory.setOnAction(this::deleteCategoryButtonOnClick);

        tfCategoryName.setText(category.getName());
    }

    private void setComponentsText() {
        this.tfCategoryName.setPromptText(LanguageResolver.getString("CATEGORY_NAME"));
        this.lblCategoryName.setText(LanguageResolver.getString("CATEGORY_NAME"));
        this.btnEditCategory.setText(LanguageResolver.getString("SAVE"));
        this.btnDeleteCategory.setText(LanguageResolver.getString("DELETE"));
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
            tfCategoryName.getStyleClass().add(LanguageResolver.getString("ERROR"));

            Platform.runLater(() -> errorLabel.setText(LanguageResolver.getString("NAME_HAST_BE_1_SYMBOL")));
        } else {
            editor.getRestManager().updateCategory(editor.getCurrentServer(), category, tfCategoryName.getText(), this);
        }
    }

    /**
     * handles the updating of a category.
     *
     * @param category category which is updated if updating was successful
     */
    public void handleEditCategory(Category category) {
        if (category != null) {
            Stage stage = (Stage) view.getScene().getWindow();
            Platform.runLater(stage::close);
            stop();
        } else {
            tfCategoryName.getStyleClass().add(LanguageResolver.getString("ERROR"));
            Platform.runLater(() -> errorLabel.setText(LanguageResolver.getString("SOMETHING_WRONG_WHILE_UPDATE_CATEGORY")));
        }
    }

    /**
     * After pressing "Delete", you get redirected to the deleteHandler
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void deleteCategoryButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.ATTENTION_SCREEN, category, null);
    }
}
