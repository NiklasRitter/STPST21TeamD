package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.model.Message;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import javax.imageio.ImageIO;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.QUOTE_ID;

public class MessageCellFactory<T extends Message> implements Callback<ListView<T>, ListCell<T>> {
    @Override
    public ListCell<T> call(ListView<T> param) {
        return new OnlineUserListCell<>(param);
    }

    private static class OnlineUserListCell<S extends Message> extends ListCell<S> {
        private final ListView<S> param;
        private final ImageView imageView = new ImageView();
        private final VBox vBox = new VBox();
        private final Label label = new Label();


        private OnlineUserListCell(ListView<S> param) {
            this.param = param;
        }

        @Override
        protected void updateItem(S item, boolean empty) {
            super.updateItem(item, empty);
            setItem(item);
            this.setText(null);
            this.getStyleClass().removeAll("font_size");
            this.setGraphic(null);

            if (!empty) {

                // set the width (-20 to eliminate overhang in ListView)
                setMinWidth(param.getWidth() - 20);
                setMaxWidth(param.getWidth() - 20);
                setPrefWidth(param.getWidth() - 20);
                setAlignment(Pos.CENTER_LEFT);

                // allow wrapping
                setWrapText(true);

                String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(item.getTimestamp()));


                if(setImgGraphic(item.getText()) && !item.getText().contains(QUOTE_PREFIX)){
                    label.setText(item.getText());
                    if(!vBox.getChildren().contains(imageView)){
                        vBox.getChildren().addAll(imageView, label);
                    }


                }else if (item.getId() != null && item.getId().equals("idLoadMore")) {
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
                    this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText());
                }

                if (item instanceof PrivateMessage) {
                    if(item.getText().startsWith("###game### System")){
                        this.setText(item.getText().substring(GAME_PREFIX.length()));
                    }else if(item.getText().startsWith(GAME_PREFIX)) {
                        this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText().substring(GAME_PREFIX.length()));
                    }
                }
            }
        }

        private boolean isValid(String url){
            try {
                //img check
                URL Url = new URL(url);
                Url.toURI();
                ImageIO.read(Url);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private boolean setImgGraphic(String url) {
            if(isValid(url)){
                Image image = new Image(url, 400,Integer.MAX_VALUE,true,false,true);
                if(!image.isError()){
                    imageView.setImage(image);
                    imageView.setPreserveRatio(true);
                    setGraphic(vBox);
                    return true;
                }
            }
            return false;
        }
    }
}


