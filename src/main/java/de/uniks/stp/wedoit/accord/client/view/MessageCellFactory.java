package de.uniks.stp.wedoit.accord.client.view;


import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Message;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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

    StageManager stageManager;

    public MessageCellFactory(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public ListCell<T> call(ListView<T> param) {
        return new MessageCell<>(param);
    }

    private class MessageCell<S extends Message> extends ListCell<S> {

        private final ListView<S> param;
        private final ImageView imageView = new ImageView();
        private MediaView mediaView = new MediaView();
        private MediaPlayer mediaPlayer;
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
            this.getStyleClass().removeAll("font_size", "marked_message");
            this.setGraphic(null);
            this.vBox.getChildren().clear();
            webView.getEngine().load(null);
            descBox.setText(null);
            hyperlink.setOnAction(null);
            hyperlink.getStyleClass().removeAll("link", "descBox");

            if (!empty) {

                // set the width (-20 to eliminate overhang in ListView)
                prefWidthProperty().bind(param.widthProperty().subtract(20));
                setMinWidth(param.getWidth() - 20);
                setMaxWidth(param.getWidth() - 20);
                setAlignment(Pos.CENTER_LEFT);

                // allow wrapping
                setWrapText(true);

                time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(item.getTimestamp()));
                if(item.getText().startsWith(GAME_PREFIX)) item.setText(item.getText().substring(GAME_PREFIX.length()));
                if (setImgGraphic(item.getText()) && !item.getText().contains(QUOTE_PREFIX)) {
                    setUpMedia(item);

                } else if (item.getId() != null && item.getId().equals("idLoadMore")) {
                    setAlignment(Pos.CENTER);
                    this.setText(item.getText());

                } else if (item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_MESSAGE)
                        && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_MESSAGE.length())
                        && (item.getText()).startsWith(QUOTE_PREFIX)) {

                    VBox messageVBox = new VBox();
                    Label quoteLabel = new Label();
                    Label messageLabel = new Label();

                    String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());

                    String[] messages = quoteMessage.split(QUOTE_MESSAGE);

                    if (messages.length != 2) {
                        this.setText(item.getText());
                    } else {
                        quoteLabel.setText(">>>" + messages[0]);
                        quoteLabel.getStyleClass().add("font_size");
                        messageLabel.setText("[" + time + "] " + item.getFrom() + ": " + messages[1]);
                        setGraphic(messageVBox);
                        messageVBox.getChildren().addAll(quoteLabel, messageLabel);
                    }

                } else if (item.getText().contains("https://ac.uniks.de/api/servers/") && item.getText().contains("/invites/")) {
                    String url = containsInviteUrl(item.getText());
                    if (url != null) {
                        setUpJoinServerView(item, url);
                    } else {
                        this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText());
                    }
                } else {
                    VBox vBox = new VBox();
                    HBox hBox = new HBox();
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    Label name = new Label(item.getFrom() + " ");

                    int nameLength = item.getFrom().length();

                    switch (nameLength % 5) {
                        case 0:
                            name.getStyleClass().add("color0");
                            break;
                        case 1:
                            name.getStyleClass().add("color1");
                            break;
                        case 2:
                            name.getStyleClass().add("color2");
                            break;
                        case 3:
                            name.getStyleClass().add("color3");
                            break;
                        case 4:
                            name.getStyleClass().add("color4");
                            break;
                    }
                    Label date = new Label(time);
                    date.getStyleClass().add("date");
                    Label text = new Label(item.getText());
                    text.getStyleClass().add("text");
                    text.setWrapText(true);
                    hBox.getChildren().addAll(name, date);
                    vBox.getChildren().addAll(hBox, text);
                    this.setGraphic(vBox);
                }

                if (item instanceof PrivateMessage) {

                    if (item.getText().startsWith(GAME_SYSTEM)) {
                        this.setText(item.getText().substring(GAME_PREFIX.length()));
                    } else if (item.getText().startsWith(GAME_PREFIX)) {
                        this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText().substring(GAME_PREFIX.length()));
                    }
                } else {
                    if (containsMarking(item.getText())) {
                        this.getStyleClass().add("marked_message");
                    }
                }
            }
        }

        private boolean containsMarking(String message) {
            if (message.contains("@" + stageManager.getEditor().getLocalUser().getName())) {
                return true;
            } else {
                return false;
            }
        }

        private String containsInviteUrl(String text) {
            if (text.contains("https://ac.uniks.de/api/servers/") && text.contains("/invites/")) {
                String[] words = text.split(" ");
                for (String word : words) {
                    if (word.contains("https://ac.uniks.de/api/servers/") && word.contains("/invites/")) {
                        return word;
                    }
                }
            }
            return null;

        }

        private boolean isValidURL(String url) {
            try {
                URL Url = new URL(url);
                Url.toURI();

                if (SUPPORTED_IMG.contains(url.substring(url.length() - 4))) return true;
                if (url.contains(MP4)) return true;
                if (url.contains(GIF)) return true;
                Document doc = Jsoup.connect(url).get();
                if (Url.getHost().equals(SUPPORTED_CLOUD) && doc.title() != null) {
                    descBox.setText(doc.title());
                    descBox.getStyleClass().add("descBox");
                }

                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private boolean setImgGraphic(String url) {
            if (isValidURL(url)) {
                if (url.contains(MP4)) {
                    setUpMediaView(url);
                    setGraphic(vBox);
                    return true;
                } else if (url.contains(GIF)) {
                    setUpWebView(url);
                    setGraphic(vBox);
                    return true;
                } else {
                    Image image = new Image(url, 370, Integer.MAX_VALUE, true, false, true);
                    if (!image.isError()) {
                        imageView.setImage(image);
                        imageView.setPreserveRatio(true);
                        setGraphic(vBox);
                        return true;
                    }
                }
            }
            return false;
        }

        private void setUpMediaView(String url) {
            Media media = new Media(url);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaView.setFitHeight(400);
            mediaView.setFitWidth(270);
            mediaView.setPreserveRatio(true);
            mediaView.setMediaPlayer(mediaPlayer);
        }

        private void setUpWebView(String url) {
            if (url == null) return;
            url = url.replace("/watch?v=", "/embed/");
            webView.setMaxWidth(400);
            webView.setMaxHeight(270);
            webView.getEngine().load(url);
        }

        private void setUpMedia(S item) {
            if (!item.getText().contains(YT_WATCH) && !item.getText().contains(YT_SHORT)) {
                if (item.getText().contains(MP4)) {
                    vBox.getChildren().addAll(label, mediaView, hyperlink);
                } else if (item.getText().contains(GIF)) {
                    vBox.getChildren().addAll(label, webView, hyperlink);
                } else {
                    vBox.getChildren().addAll(label, imageView, mediaView, hyperlink);
                }
                if (descBox.getText() != null) {
                    vBox.getChildren().add(descBox);
                    descBox.setOnAction(this::openHyperLink);
                }

            } else if (item.getText().contains(YT_WATCH) || item.getText().contains(YT_SHORT)) {
                if (item.getText().contains(YT_SHORT)) setUpWebView(expandUrl(item.getText()));
                else setUpWebView(item.getText());
                vBox.getChildren().addAll(label, webView, hyperlink);
            }

            label.setText("[" + time + "] " + item.getFrom() + ": ");
            hyperlink.setText(item.getText());
            hyperlink.getStyleClass().add("link");
            hyperlink.setOnAction(this::openHyperLink);
        }

        public String expandUrl(String shortenedUrl) {
            return YT_PREFIX + shortenedUrl.substring(shortenedUrl.lastIndexOf("/") + 1);
        }

        private void openHyperLink(ActionEvent actionEvent) {
            openBrowser(hyperlink.getText());
        }

        private void openBrowser(String url) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

        private void setUpJoinServerView(Message item, String url) {
            Label enterServerLabel = new Label();
            enterServerLabel.setText(LanguageResolver.getString("ENTER_SERVER"));
            Label serverIdLabel = new Label();
            String[] urlSplitted = url.split("/");
            String serverId = urlSplitted[5];
            serverIdLabel.setText(LanguageResolver.getString("SERVER_ID") + ": " + serverId);
            serverIdLabel.setWrapText(true);

            VBox serverInfoVBox = new VBox();
            serverInfoVBox.getChildren().addAll(enterServerLabel, serverIdLabel);

            Button button = new Button();
            button.setText(LanguageResolver.getString("JOIN"));
            button.getStyleClass().add("styleButton");
            button.setOnAction(event -> joinButtonOnClick(url));

            Region region = new Region();
            region.setPrefWidth(15);

            HBox joinServerHBox = new HBox();
            joinServerHBox.setAlignment(Pos.CENTER_LEFT);
            joinServerHBox.getChildren().addAll(serverInfoVBox, region, button);
            joinServerHBox.getStyleClass().add("styleBorder");
            joinServerHBox.setMaxWidth(265);

            label.setText("[" + time + "] " + item.getFrom() + ": ");

            Label textLabel = new Label(item.getText());
            textLabel.setWrapText(true);

            this.vBox.getChildren().addAll(this.label, joinServerHBox, textLabel);
            setGraphic(this.vBox);
        }

    }


    private void joinButtonOnClick(String inviteLink) {
        stageManager.getEditor().getRestManager().joinServer(stageManager.getEditor().getLocalUser(), inviteLink, this);
    }

    public void handleInvitation(Server server, String responseMessage) {
        if (server != null) {
            Platform.runLater(() -> this.stageManager.initView(ControllerEnum.SERVER_SCREEN, server, null));
        } else {
            if (responseMessage.equals("MainScreen")) {
                Platform.runLater(() -> this.stageManager.initView(ControllerEnum.MAIN_SCREEN, null, null));
            }
        }
    }

}


