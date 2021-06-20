package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;

public class PrivateMessageCellFactory implements javafx.util.Callback<ListView<PrivateMessage>, ListCell<PrivateMessage>> {

    @Override
    public ListCell<PrivateMessage> call(ListView<PrivateMessage> param) {
        return new OnlineUserListCell(param);
    }

    private class OnlineUserListCell extends ListCell<PrivateMessage> {

        private final ListView<PrivateMessage> param;

        private OnlineUserListCell(ListView<PrivateMessage> param) {
            this.param = param;
        }

        protected void updateItem(PrivateMessage item, boolean empty) {
            super.updateItem(item, empty);
            this.getStyleClass().removeAll("font_size");
            if (!empty) {

                // set the width (-20 to eliminate overhang in ListView)
                setMinWidth(param.getWidth() - 20);
                setMaxWidth(param.getWidth() - 20);
                setPrefWidth(param.getWidth() - 20);
                setAlignment(Pos.CENTER_LEFT);

                // allow wrapping
                setWrapText(true);

                String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(item.getTimestamp()));

                // handle quotes
                if (item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_ID)
                        && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_ID.length())
                        && (item.getText()).startsWith(QUOTE_PREFIX)) {
                    String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());

                    String[] messages = quoteMessage.split(QUOTE_ID);

                    this.getStyleClass().add("font_size");
                    this.setText(">>>" + messages[0] + "\n");
                }else if(item.getText().startsWith("###game### System")){
                    this.setText(item.getText().substring(GAME_PREFIX.length()));
                }else if(item.getText().startsWith(GAME_PREFIX)) {
                    this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText().substring(GAME_PREFIX.length()));
                }else if (item.getId() != null && item.getId().equals("idLoadMore")) {
                    setAlignment(Pos.CENTER);
                    this.setText(item.getText());
                }{
                    this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText());
                }
            } else {
                this.setText("");
                this.getStyleClass().removeAll("font_size");
            }
        }
    }
}