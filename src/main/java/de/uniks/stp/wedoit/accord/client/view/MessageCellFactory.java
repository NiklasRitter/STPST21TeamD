package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Message;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.uniks.stp.wedoit.accord.client.constants.ChatMedia.*;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_SYSTEM;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;

public class MessageCellFactory<T extends Message> implements Callback<ListView<T>, ListCell<T>> {
    @Override
    public ListCell<T> call(ListView<T> param) {
        return new MessageCell<>(param);
    }

    private static class MessageCell<S extends Message> extends ListCell<S> {
        private final ListView<S> param;
        private final ImageView imageView = new ImageView();
        private final VBox vBox = new VBox();
        private final Label label = new Label();
        private final Hyperlink hyperlink = new Hyperlink(), descBox = new Hyperlink();
        private final WebView webView = new WebView();
        private String time;

        private MessageCell(ListView<S> param) {
            this.param = param;
        }

        @Override
        protected void updateItem(S item, boolean empty) {
            super.updateItem(item, empty);
            setItem(item);
            this.setText(null);
            this.getStyleClass().removeAll("font_size");
            this.setGraphic(null);
            this.vBox.getChildren().clear();
            webView.getEngine().load(null);
            descBox.setText(null);
            hyperlink.setOnAction(null);
            hyperlink.getStyleClass().removeAll("link","descBox");


            if (!empty) {

                // set the width (-20 to eliminate overhang in ListView)
                setMinWidth(param.getWidth() - 20);
                setMaxWidth(param.getWidth() - 20);
                setPrefWidth(param.getWidth() - 20);
                setAlignment(Pos.CENTER_LEFT);

                // allow wrapping
                setWrapText(true);

                time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(item.getTimestamp()));


                if (setImgGraphic(item.getText()) && !item.getText().contains(QUOTE_PREFIX)) {
                    setUpMedia(item);

                } else if (item.getId() != null && item.getId().equals("idLoadMore")) {
                    setAlignment(Pos.CENTER);
                    this.setText(item.getText());

                } else if (item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_ID)
                        && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_ID.length())
                        && (item.getText()).startsWith(QUOTE_PREFIX)) {

                    String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());

                    String[] messages = quoteMessage.split(QUOTE_ID);

                    this.getStyleClass().add("font_size");
                    this.setText(">>>" + messages[0] + "\n");

                } else if (item.getText().contains("https://ac.uniks.de/api/servers/") && item.getText().contains("/invites/")) {
                    setUpJoinServerView(item);
                } else {
                    this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText());
                }

                if (item instanceof PrivateMessage) {
                    if (item.getText().startsWith(GAME_SYSTEM)) {
                        this.setText(item.getText().substring(GAME_PREFIX.length()));
                    } else if (item.getText().startsWith(GAME_PREFIX)) {
                        this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText().substring(GAME_PREFIX.length()));
                    }
                }
            }
        }

        private boolean isValidURL(String url) {
            try {
                URL Url = new URL(url);
                Url.toURI();

                if(SUPPORTED_IMG.contains(url.substring(url.length()-4))) return true;

                Document doc = Jsoup.connect(url).get();
                if(Url.getHost().equals(SUPPORTED_CLOUD) && doc.title() != null){
                    descBox.setText(doc.title());
                    descBox.getStyleClass().add("descBox");
                }

                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private boolean setImgGraphic(String url) {
            if(isValidURL(url)){
                Image image = new Image(url, 370,Integer.MAX_VALUE,true,false,true);
                if(!image.isError()){
                    imageView.setImage(image);
                    imageView.setPreserveRatio(true);
                    setGraphic(vBox);
                    return true;
                }
            }
            return false;
        }

        private void setUpWebView(String url){
            if(url == null) return;
            url = url.replace("/watch?v=","/embed/");
            webView.setMaxWidth(400);
            webView.setMaxHeight(270);
            webView.getEngine().load(url);
        }

        private void setUpMedia(S item){
            if (!item.getText().contains(YT_WATCH) && !item.getText().contains(YT_SHORT)) {
                vBox.getChildren().addAll(label, imageView, hyperlink);
                if(descBox.getText() != null){
                    vBox.getChildren().add(descBox);
                    descBox.setOnAction(this::openHyperLink);
                }

            } else if (item.getText().contains(YT_WATCH) || item.getText().contains(YT_SHORT)) {
                if(item.getText().contains(YT_SHORT)) setUpWebView(expandUrl(item.getText()));
                else setUpWebView(item.getText());
                vBox.getChildren().addAll(label, webView, hyperlink);
            }

            label.setText("[" + time + "] " + item.getFrom() + ": ");
            hyperlink.setText(item.getText());
            hyperlink.getStyleClass().add("link");
            hyperlink.setOnAction(this::openHyperLink);
        }

        private void setUpJoinServerView(S item) {
            Label enterServerLabel = new Label();
            enterServerLabel.setText(LanguageResolver.getString("ENTER_SERVER"));
            Label serverIdLabel = new Label();
            int indexServer = item.getText().indexOf("servers/");
            int indexInvite = item.getText().indexOf("/invites");
            String serverId = item.getText().substring(indexServer + "servers/".length(), indexInvite);
            serverIdLabel.setText(LanguageResolver.getString("SERVER_ID") + ": " + serverId);

            VBox textVbox = new VBox();
            textVbox.getChildren().addAll(enterServerLabel, serverIdLabel);

            Button button = new Button();
            button.setText(LanguageResolver.getString("JOIN"));
            button.getStyleClass().add("styleButton");
            //button.setOnAction();

            Region region = new Region();
            region.setPrefWidth(30);

            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.getChildren().addAll(textVbox, region, button);

            label.setText("[" + time + "] " + item.getFrom() + ": ");
            hyperlink.setText(item.getText());
            hyperlink.getStyleClass().add("link");
            //hyperlink.setOnAction(this::openHyperLink);
            vBox.getChildren().addAll(label, hBox, hyperlink);
            vBox.getStyleClass().add("styleBorder");
            setGraphic(this.vBox);
        }

        public String expandUrl(String shortenedUrl){
            return YT_PREFIX + shortenedUrl.substring(shortenedUrl.lastIndexOf("/")+1);
        }

        private void openHyperLink(ActionEvent actionEvent) {
            openBrowser(hyperlink.getText());
        }

        private void openBrowser(String url){
            if(Desktop.isDesktopSupported()){
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}


