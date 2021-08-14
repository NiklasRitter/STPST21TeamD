package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.richtext.RichTextArea;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import static de.uniks.stp.wedoit.accord.client.constants.UserDescription.CUSTOM_KEY;

public class DescriptionController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;
    private VBox vBoxDescription;
    private RichTextArea richTextArea = new RichTextArea();

    public DescriptionController(Parent view, Options model, Editor editor) {
        this.view = view;
        this.options = model;
        this.editor = editor;
    }

    @Override
    public void init() {
        this.vBoxDescription = (VBox) view.lookup("#vBoxDescription");
        richTextArea.setId("rTArea");
        vBoxDescription.setMaxWidth(500);
        for (User user : editor.getLocalUser().getUsers()) {
            if (user.getName().equals(editor.getLocalUser().getName()) && user.getDescription() != null) {
                editor.getLocalUser().setDescription(user.getDescription());
                richTextArea.setText(user.getDescription());
            }
        }
        vBoxDescription.getChildren().add(richTextArea);

    }

    @Override
    public void stop() {
        if (richTextArea.getText() != null) {
            options.getAccordClient().getLocalUser().setDescription(JsonUtil.buildDescription(CUSTOM_KEY, richTextArea.getText()));
        }
    }
}
