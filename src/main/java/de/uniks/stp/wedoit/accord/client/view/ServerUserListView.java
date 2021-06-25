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

public class ServerUserListView implements javafx.util.Callback<ListView<User>, ListCell<User>> {

    private final StageManager stageManager;
    private final Server server;

    @Override
    public ListCell<User> call(ListView<User> param) {
        return new UserListCell();
    }

    public ServerUserListView(StageManager stageManager, Server server) {
        this.stageManager = stageManager;
        this.server = server;
    }


    private class UserListCell extends ListCell<User> {
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            this.setGraphic(null);
            this.setText(null);
            Circle circle = new Circle(4);
            circle.setFill(Color.TRANSPARENT);

            if (!empty) {
                this.setText(item.getName());
                this.setGraphic(circle);
                if (!stageManager.getEditor().getLocalUser().getName().equals(item.getName())) {
                    this.setContextMenu(createContextMenuWriteMembers(item));
                }
                if (item.isOnlineStatus()) {

                    circle.setFill(Color.GREEN);
                } else {
                    this.setText(item.getName());
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
        menuItemWriteMembers.setOnAction((event) -> stageManager.showPrivateMessageServerScreen(server, user));
        return contextMenu;
    }


}
