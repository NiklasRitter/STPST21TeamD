package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.PRIVATE_MESSAGE_SERVER_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUPSTAGE;

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
            if(isPrivate) this.getStyleClass().removeAll("newMessage");

            if (!empty && item != null) {
                this.setGraphic(circle);
                this.setText(item.getName());
                if (isPrivate && !item.isChatRead()) {
                    this.getStyleClass().add("newMessage");
                }
                if (!isPrivate && !stageManager.getEditor().getLocalUser().getName().equals(item.getName())) {
                    this.setContextMenu(createContextMenuWriteMembers(item));
                }
                if(item.isOnlineStatus()){
                    circle.setFill(Color.GREEN);
                }else{
                    circle.setFill(Color.RED);
                }
            } else {
                this.setContextMenu(null);
            }
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
        menuItemWriteMembers.setOnAction((event) -> stageManager.initView(POPUPSTAGE, user.getName(), "PrivateMessageServerScreen", PRIVATE_MESSAGE_SERVER_SCREEN_CONTROLLER, false, server, user));
        return contextMenu;
    }
}