package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

public class OnlineUsersCellFactory implements Callback<ListView<User>, ListCell<User>> {
    private boolean isPrivate;

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

                if(item.isOnlineStatus()){
                    circle.setFill(Color.GREEN);
                }else{
                    circle.setFill(Color.RED);
                }
            }
        }
    }
}