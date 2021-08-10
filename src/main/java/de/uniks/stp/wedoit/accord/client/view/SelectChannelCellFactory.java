package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.Channel;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class SelectChannelCellFactory implements Callback<ListView<Channel>, ListCell<Channel>> {

    @Override
    public ListCell<Channel> call(ListView<Channel> param) {
        return new UserListCell();
    }

    private class UserListCell extends ListCell<Channel> {
        @Override
        protected void updateItem(Channel item, boolean empty) {
            super.updateItem(item, empty);
            this.setText(null);
            this.setGraphic(null);

            if (!empty && item != null) {
                this.setText("#" + item.getName());
            }
        }
    }
}