package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Category;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateCategoryScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private TextField tfCategoryName;
    private Button btnCreateCategory;
    private Label errorLabel;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param editor The editor of the Application
     */
    public CreateCategoryScreenController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
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
        this.btnCreateCategory = (Button) view.lookup("#btnCreateCategory");
        this.tfCategoryName = (TextField) view.lookup("#tfCategoryName");
        this.errorLabel = (Label) view.lookup("#lblError");

        // Add action listeners
        this.btnCreateCategory.setOnAction(this::createCategoryButtonOnClick);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        // Remove all action listeners
        btnCreateCategory.setOnAction(null);
    }


    /**
     * After pressing "Create", the category will be created with the name in the text field and you get
     * redirected to the Screen for the Server
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void createCategoryButtonOnClick(ActionEvent actionEvent) {
        if (tfCategoryName.getText().length() < 1 || tfCategoryName.getText() == null) {
            tfCategoryName.getStyleClass().add("error");

            Platform.runLater(() -> errorLabel.setText("Name has to be at least 1 symbols long"));
        } else {
            editor.getRestManager().createCategory(editor.getCurrentServer(), tfCategoryName.getText(), this);
        }
    }

    /**
     * handles the creation of a category.
     *
     * @param category the category which should be created
     */
    public void handleCreateCategory(Category category) {
        if (category != null) {
            Stage stage = (Stage) view.getScene().getWindow();
            Platform.runLater(stage::close);
            stop();
        } else {
            tfCategoryName.getStyleClass().add("error");
            Platform.runLater(() -> errorLabel.setText("Something went wrong while creating the category"));
        }
    }
}
