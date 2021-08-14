package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Options;
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

    public DescriptionController(Parent view, Options model, Editor editor){
        this.view = view;
        this.options = model;
        this.editor = editor;
    }

    @Override
    public void init() {
        System.out.println(options.getAccordClient().getLocalUser().getUserKey());
        System.out.println(options.getAccordClient().getLocalUser().getId());
        this.vBoxDescription = (VBox) view.lookup("#vBoxDescription");
        vBoxDescription.setMaxWidth(500);

        vBoxDescription.getChildren().add(richTextArea);

    }

    @Override
    public void stop() {
        System.out.println("Called stop");
        if (richTextArea.getText() != null) {
            options.getAccordClient().getLocalUser().setDescription(JsonUtil.buildDescription(CUSTOM_KEY, richTextArea.getText()));
        }
    }
}
