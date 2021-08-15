package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import static de.uniks.stp.wedoit.accord.client.constants.UserDescription.CUSTOM_KEY;

public class DescriptionController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;
    private VBox vBoxDescription;
    private final TextField textfield = new TextField();

    public DescriptionController(Parent view, Options model, Editor editor) {
        this.view = view;
        this.options = model;
        this.editor = editor;
    }

    @Override
    public void init() {
        this.vBoxDescription = (VBox) view.lookup("#vBoxDescription");
        textfield.setId("rTArea");
        textfield.setStyle("-fx-text-fill: WHITE");
        textfield.setPrefWidth(450);
        vBoxDescription.setMaxWidth(500);
        for (User user : editor.getLocalUser().getUsers()) {
            if (user.getName().equals(editor.getLocalUser().getName()) && user.getDescription() != null) {
                editor.getLocalUser().setDescription(user.getDescription());
            }
        }
        if (editor.getLocalUser().getDescription() != null && editor.getLocalUser().getDescription().length() > 0 && editor.getLocalUser().getDescription().startsWith("+")) {
            textfield.setText(editor.getLocalUser().getDescription().substring(1));
        }
        vBoxDescription.getChildren().add(textfield);
    }

    @Override
    public void stop() {
        if (textfield.getText() != null) {
            options.getAccordClient().getLocalUser().setDescription(JsonUtil.buildDescription(CUSTOM_KEY, textfield.getText()));
        }
    }
}
