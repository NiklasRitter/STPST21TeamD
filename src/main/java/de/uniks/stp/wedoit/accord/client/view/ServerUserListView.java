package de.uniks.stp.wedoit.accord.client.view;


import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ServerUserListView implements javafx.util.Callback<ListView<User>, ListCell<User>> {

    @Override
    public ListCell<User> call(ListView<User> param) {
        return new UserListCell();
    }


    private class UserListCell extends ListCell<User> {
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                if (item.isOnlineStatus()) {
                    this.setText(item.getName() + " [online]");
                    this.getStyleClass().add("listViewUserOnline");
                } else {
                    this.setText(item.getName() + " [offline]");
                    this.getStyleClass().add("listViewUserOffline");
                }
            } else {
                this.setText(null);
                this.getStyleClass().removeAll("listViewUserOffline", "listViewUserOnline");
            }
        }
    }

}
