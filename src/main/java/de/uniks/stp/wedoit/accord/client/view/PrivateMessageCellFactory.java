package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class PrivateMessageCellFactory implements javafx.util.Callback<ListView<PrivateMessage>, ListCell<PrivateMessage>> {

    @Override
    public ListCell<PrivateMessage> call(ListView<PrivateMessage> param) {
        return new OnlineUserListCell(param);
    }

    private static class OnlineUserListCell extends ListCell<PrivateMessage> {

        private final ListView<PrivateMessage> param;

        private OnlineUserListCell(ListView<PrivateMessage> param) {
            this.param = param;
        }

        protected void updateItem(PrivateMessage item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                // set the width's
                setMinWidth(param.getWidth());
                setMaxWidth(param.getWidth());
                setPrefWidth(param.getWidth());

                // allow wrapping
                setWrapText(true);
                this.setText("[" + item.getTimestamp() + "] " + item.getFrom() + ": " + item.getText());
            } else {
                this.setText("");
            }
        }
    }
}