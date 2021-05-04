package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class WelcomeScreenOnlineUsersListView implements javafx.util.Callback<ListView<User>, ListCell<User>> {

    @Override
    public ListCell<User> call(ListView<User> param) {
        return new OnlineUserListCell();
    }

    private static class OnlineUserListCell extends ListCell<User> {
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                this.setText(item.getName());
            } else {
                this.setText(null);
            }
        }
    }
}