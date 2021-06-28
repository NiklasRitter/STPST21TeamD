package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.ServerChatController;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.Message;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Date;

import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;

public class MessageCellFactory implements javafx.util.Callback<ListView<Message>, ListCell<Message>> {

    private final Editor editor;
    private final ServerChatController serverChatController;

    public MessageCellFactory(Editor editor, ServerChatController serverChatController) {
        this.editor = editor;
        this.serverChatController = serverChatController;
    }

    @Override
    public ListCell<Message> call(ListView<Message> param) {
        return new MessageChatCell(param);
    }

    private class MessageChatCell extends ListCell<Message> {

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
                } else if (item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_ID)
                        && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_ID.length())
                        && (item.getText()).startsWith(QUOTE_PREFIX)) {
                    String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());

                    String[] messages = quoteMessage.split(QUOTE_ID);

                    this.getStyleClass().add("font_size");
                    this.setText(">>>" + messages[0] + "\n");

                } else {
                    String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(item.getTimestamp()));
                    this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText());
                }

            } else {
                this.setText("");
                this.getStyleClass().removeAll("font_size");
            }
        }
    }
}