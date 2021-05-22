package de.uniks.stp.wedoit.accord.client.view;


import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ServerUserListView implements javafx.util.Callback<ListView<User>, ListCell<User>> {

    @Override
    public ListCell<User> call(ListView<User> param) {
        return new UserListCell();
    }


    private static class UserListCell extends ListCell<User> {
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                if (item.isOnlineStatus()) {
                    this.setText(item.getName() + " [online]");
                    this.setStyle("-fx-border-color: green");
                } else {
                    this.setText(item.getName() + " [offline]");
                    this.setStyle("-fx-border-color: red");
                }
            } else {
                this.setText(null);
                this.setStyle(null);
            }
        }
    }

}
