package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.scene.Parent;

public class DescriptionController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;

    public DescriptionController(Parent view, Options model, Editor editor){
        this.view = view;
        this.options = model;
        this.editor = editor;
    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }
}
