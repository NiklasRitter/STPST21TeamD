package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

public class OnlineUsersCellFactory implements Callback<ListView<User>, ListCell<User>> {
    private boolean isPrivate;
    private final StageManager stageManager;
    private final Server server;

    public OnlineUsersCellFactory(StageManager stageManager, Server server) {
        this.stageManager = stageManager;
        this.server = server;
    }

    @Override
    public ListCell<User> call(ListView<User> param) {
        isPrivate = param.getId().equals("lwOnlineUsers");
        return new OnlineUserListCell();
    }

    private class OnlineUserListCell extends ListCell<User> {
        @Override
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            this.setText(null);
            this.setGraphic(null);
            Circle circle = new Circle(4);
            circle.setFill(Color.TRANSPARENT);
            if (isPrivate) this.getStyleClass().removeAll("newMessage");

            if (!empty && item != null) {
                if (isPrivate) {
                    this.setGraphic(circle);
                    this.setText(item.getName());
                } else {
                    HBox hBox = new HBox();
                    VBox vBox = new VBox();
                    Label name = new Label(item.getName());
                    name.setPadding(new Insets(0, 0, 0, 3));
                    Label description = new Label(item.getDescription());
                    description.setTooltip(new Tooltip(item.getDescription()));
                    setLabelStyle(name, description);


                    hBox.setAlignment(Pos.CENTER_LEFT);
                    vBox.setAlignment(Pos.CENTER_LEFT);

                    hBox.getChildren().addAll(circle, name);
                    vBox.getChildren().addAll(hBox, description);
                    this.setGraphic(vBox);
                }

                if (isPrivate && !item.isChatRead()) {
                    this.getStyleClass().add("newMessage");
                }
                if (!isPrivate && !stageManager.getEditor().getLocalUser().getName().equals(item.getName())) {
                    this.setContextMenu(createContextMenuWriteMembers(item));
                }
                if (item.isOnlineStatus()) {
                    circle.setFill(Color.GREEN);
                } else {
                    circle.setFill(Color.RED);
                }
            } else {
                this.setContextMenu(null);
            }
        }
    }

    private void setLabelStyle(Label name, Label description) {
        description.setStyle("-fx-font-size: 10");
        if (stageManager.getPrefManager().loadDarkMode()) {
            name.setStyle("-fx-text-fill: #ADD8e6");
            description.setStyle("-fx-text-fill: #ADD8e6");
        } else {
            name.setStyle("-fx-text-fill: #000000");
            description.setStyle("-fx-text-fill: #000000");
        }
    }

    /**
     * creates a context menu to write a private Message to a member
     *
     * @return the created context menu
     */
    private ContextMenu createContextMenuWriteMembers(User user) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemWriteMembers = new MenuItem("Private Message");
        contextMenu.getItems().add(menuItemWriteMembers);
        menuItemWriteMembers.setOnAction((event) -> stageManager.initView(ControllerEnum.PRIVATE_MESSAGE_SERVER_SCREEN, server, user));
        return contextMenu;
    }
}