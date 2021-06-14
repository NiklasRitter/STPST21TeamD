package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.Message;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.TextAlignment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageCellFactory implements javafx.util.Callback<ListView<Message>, ListCell<Message>> {

    @Override
    public ListCell<Message> call(ListView<Message> param) {
        return new MessageChatCell(param);
    }

    private static class MessageChatCell extends ListCell<Message> {

        private final ListView<Message> param;

        private MessageChatCell(ListView<Message> param) {
            this.param = param;
        }

        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                // set the width's
                setMinWidth(param.getWidth() - 20);
                setMaxWidth(param.getWidth() - 20);
                setPrefWidth(param.getWidth() - 20);
                setAlignment(Pos.CENTER_LEFT);

                // allow wrapping
                setWrapText(true);

                if (item.getId() != null && item.getId().equals("idLoadMore")) {
                    setAlignment(Pos.CENTER);
                    this.setText(item.getText());
                } else {
                    String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(item.getTimestamp()));
                    this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText());
                }
            } else {
                this.setText("");
            }
        }
    }
}