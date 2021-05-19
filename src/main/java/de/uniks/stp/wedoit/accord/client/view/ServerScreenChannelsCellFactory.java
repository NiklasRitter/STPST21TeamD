package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ServerScreenChannelsCellFactory implements javafx.util.Callback<ListView<Channel>, ListCell<Channel>> {

    @Override
    public ListCell<Channel> call(ListView<Channel> param) {
        return new ChannelListCell();
    }

    private static class ChannelListCell extends ListCell<Channel> {
        protected void updateItem(Channel item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                this.setText(item.getName());
            } else {
                this.setText(null);
            }
        }
    }
}