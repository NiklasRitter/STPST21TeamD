package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class MemberListSubViewController implements Controller {

    private final User user;
    private final Parent view;
    private final Editor editor;
    private final Category category;
    private HBox hBoxPlaceHolder;
    private VBox vBoxMemberName, vBoxCheckBox;
    private CheckBox checkBox;

    public MemberListSubViewController(User user, Parent view, Editor editor, Category category) {
        this.user = user;
        this.view = view;
        this.editor = editor;
        this.category = category;
    }

    @Override
    public void init() {
        this.hBoxPlaceHolder = (HBox) this.view.lookup("#hBoxPlaceHolder");
        this.vBoxMemberName = (VBox) this.view.lookup("#vBoxMemberName");
        this.vBoxCheckBox = (VBox) this.view.lookup("#vBoxCheckBox");

        this.checkBox = new CheckBox();
        this.checkBox.setOnAction(this::checkBoxOnClick);
        this.vBoxMemberName.getChildren().add(new Label(user.getName()));
        this.vBoxCheckBox.getChildren().add(checkBox);
    }

    private void checkBoxOnClick(ActionEvent actionEvent) {
        if (checkBox.isSelected()) {

        }
    }

    @Override
    public void stop() {
        this.checkBox.setOnAction(null);
    }
}
