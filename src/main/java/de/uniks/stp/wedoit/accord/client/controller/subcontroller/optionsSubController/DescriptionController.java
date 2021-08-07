package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.richtext.RichTextArea;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

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
        this.vBoxDescription = (VBox) view.lookup("#vBoxDescription");

        vBoxDescription.getChildren().add(richTextArea);

    }

    @Override
    public void stop() {

    }
}
