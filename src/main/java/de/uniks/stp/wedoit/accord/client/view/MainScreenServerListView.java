package de.uniks.stp.wedoit.accord.client.view;


import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

public class MainScreenServerListView implements javafx.util.Callback<ListView<Server>, ListCell<Server>> {

    private final Editor editor;

    public MainScreenServerListView(Editor editor) {
        this.editor = editor;
    }

    @Override
    public ListCell<Server> call(ListView<Server> param) {
        return new ServerListCell();
    }

    public ContextMenu addContextMenu(Server item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("- " + LanguageResolver.getString("LEAVE_SERVER"));
        contextMenu.getItems().add(menuItem1);
        menuItem1.setOnAction((event) -> this.editor.getStageManager().initView(ControllerEnum.ATTENTION_LEAVE_SERVER_SCREEN, item, null));

        return contextMenu;
    }

    private class ServerListCell extends ListCell<Server> {
        protected void updateItem(Server item, boolean empty) {
            super.updateItem(item, empty);
            this.setText(null);
            this.setGraphic(null);

            if (!empty) {
                Circle circle = new Circle(20);
                Text text = new Text(getShortName(item.getName()));

                if (isSelected()) {
                    circle.getStyleClass().add("serverCircleSelected");
                    text.getStyleClass().add("serverCircleTextSelected");
                } else {
                    circle.getStyleClass().add("serverCircle");
                    text.getStyleClass().add("serverCircleText");
                }

                this.setTooltip(new Tooltip(item.getName()));

                text.setBoundsType(TextBoundsType.VISUAL);
                StackPane stack = new StackPane();
                stack.getChildren().addAll(circle, text);
                this.setGraphic(stack);
                this.setContextMenu(addContextMenu(item));
            } else {
                this.setText(null);
            }
        }

        private String getShortName(String name) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                if (result.length() > 2) {
                    return result.toString().toUpperCase();
                }
                if (i == 0) {
                    result.append(name.charAt(i));
                } else {
                    if (name.charAt(i) == ' ' && name.length() > i + 1) {
                        result.append(name.charAt(i + 1));
                    }
                }
            }
            return result.toString().toUpperCase();
        }
    }

}
