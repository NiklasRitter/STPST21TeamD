package de.uniks.stp.wedoit.accord.client.view;


import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ServerUserListView implements javafx.util.Callback<ListView<User>, ListCell<User>> {

    @Override
    public ListCell<User> call(ListView<User> param) {
        return new UserListCell();
    }


    private static class UserListCell extends ListCell<User> {
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            this.setGraphic(null);
            this.setText(null);
            Circle circle = new Circle(4);
            circle.setFill(Color.TRANSPARENT);

            if (!empty) {
                this.setText(item.getName());
                this.setGraphic(circle);
                if (item.isOnlineStatus()) {

                    circle.setFill(Color.GREEN);
                } else {
                    this.setText(item.getName());
                    circle.setFill(Color.RED);
                }
            }
        }
    }

}
