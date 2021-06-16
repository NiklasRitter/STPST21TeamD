package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PrivateChatsScreenOnlineUsersCellFactory implements javafx.util.Callback<ListView<User>, ListCell<User>> {

    @Override
    public ListCell<User> call(ListView<User> param) {
        return new OnlineUserListCell();
    }
    private static class OnlineUserListCell extends ListCell<User> {
        @Override
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);
            this.setText(null);
            this.setGraphic(null);
            Circle circle = new Circle(4);
            circle.setFill(Color.TRANSPARENT);
            this.getStyleClass().remove("newMessage");
            if (!empty && item != null) {
                this.setGraphic(circle);
                this.setText(item.getName());
                if (!item.isChatRead()) {
                    this.getStyleClass().add("newMessage");
                }

                if(item.isOnlineStatus()){
                    circle.setFill(Color.GREEN);
                }else{
                    circle.setFill(Color.RED);
                }
            }
        }
    }
}