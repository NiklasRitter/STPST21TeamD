package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;


public class SelectUserCellFactory implements Callback<ListView<User>, ListCell<User>> {

    @Override
    public ListCell<User> call(ListView<User> param) {
        return new UserListCell();
    }

    private class UserListCell extends ListCell<User> {
        @Override
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            this.setText(null);
            this.setGraphic(null);
            Circle circle = new Circle(4);
            circle.setFill(Color.TRANSPARENT);

            if (!empty && item != null) {
                this.setGraphic(circle);
                this.setText("@" + item.getName());

                if (item.isOnlineStatus()) {
                    circle.setFill(Color.GREEN);
                } else {
                    circle.setFill(Color.RED);
                }
            }
        }
    }
}