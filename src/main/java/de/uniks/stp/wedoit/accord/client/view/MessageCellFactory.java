package de.uniks.stp.wedoit.accord.client.view;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Message;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.util.EmojiTextFlowParameterHelper;
import javafx.application.Platform;
import javafx.css.Style;
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
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

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
        private final ImageView imgVwBtnHandleMedia = new ImageView();
        private final Image imagePlay = new Image(Objects.requireNonNull(MessageCellFactory.class.getResourceAsStream("images/play.png")));
        private final Image imageStop = new Image(Objects.requireNonNull(MessageCellFactory.class.getResourceAsStream("images/stop.png")));
        private Button btnHandleMedia = new Button();
        private Label lblDate = new Label();
        private MediaView mediaView = new MediaView();
        private MediaPlayer mediaPlayer;
        private final VBox vBox = new VBox();
        private final Label label = new Label();
        private Label lblTime = new Label();
        private final Hyperlink hyperlink = new Hyperlink(), descBox = new Hyperlink();
        private final WebView webView = new WebView();
        private String time;
        private EmojiTextFlowParameters parameters;
        private EmojiTextFlowParameters parametersQuote;
        private EmojiTextFlow emojiTextFlow;
        private EmojiTextFlow quoteTextFlow;

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

            // parameters for emoji
            this.parametersQuote = new EmojiTextFlowParameterHelper(stageManager.getEditor().getFontSize() -3).createParameters();
            this.parameters = new EmojiTextFlowParameterHelper(stageManager.getEditor().getFontSize()).createParameters();
            if (stageManager.getPrefManager().loadDarkMode()) {
                this.parameters.setTextColor(Color.valueOf("#ADD8e6"));
                this.parametersQuote.setTextColor(Color.valueOf("#ADD8e6"));
                this.hyperlink.setStyle("-fx-text-fill: #ADD8e6");
                this.label.setStyle("-fx-text-fill: #ADD8e6");
            } else {
                this.parameters.setTextColor(Color.valueOf("#000000"));
                this.parametersQuote.setTextColor(Color.valueOf("#000000"));
                this.hyperlink.setStyle("-fx-text-fill: #000000");
                this.label.setStyle("-fx-text-fill: #000000");
            }
            emojiTextFlow = new EmojiTextFlow(this.parameters);
            quoteTextFlow = new EmojiTextFlow(this.parametersQuote);

            if (!empty) {

                // set the width (-20 to eliminate overhang in ListView)
                prefWidthProperty().bind(param.widthProperty().subtract(20));
                setMinWidth(param.getWidth() - 20);
                setMaxWidth(param.getWidth() - 20);
                setAlignment(Pos.CENTER_LEFT);

                // allow wrapping
                setWrapText(true);

                if(item.getText().startsWith(GAME_PREFIX)) item.setText(item.getText().substring(GAME_PREFIX.length()));

                time = checkTime(item);

                if (item instanceof PrivateMessage) {
                    if (item.getText().startsWith(GAME_SYSTEM)) {
                        this.setText(item.getText().substring(GAME_PREFIX.length()));
                        return;
                    } else if (item.getText().startsWith(GAME_PREFIX)) {
                        this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText().substring(GAME_PREFIX.length()));
                        return;
                    }
                }

                if (setImgGraphic(item.getText()) && !item.getText().contains(QUOTE_PREFIX)) {
                    setUpMedia(item);

                } else if (item.getId() != null && item.getId().equals("idLoadMore")) {
                    setAlignment(Pos.CENTER);
                    this.setText(item.getText());

                } else if (item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_MESSAGE)
                        && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_MESSAGE.length())
                        && (item.getText()).startsWith(QUOTE_PREFIX)) {

                    String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());

                    String[] messages = quoteMessage.split(QUOTE_MESSAGE);

                    if (messages.length != 2) {
                        this.setText(item.getText());
                    } else {
                        displayNameAndDate(item);
                        quoteTextFlow.parseAndAppend(">>> " + messages[0]);
                        this.vBox.getChildren().add(quoteTextFlow);
                        this.emojiTextFlow.parseAndAppend(messages[1]);
                        this.vBox.getChildren().add(emojiTextFlow);
                        setGraphic(vBox);
                    }

                } else if (item.getText().contains("https://ac.uniks.de/api/servers/") && item.getText().contains("/invites/")) {
                    String url = containsInviteUrl(item.getText());
                    if (url != null) {
                        setUpJoinServerView(item, url);
                    } else {
                        this.setStyle("-fx-font-size: 12");
                        displayNameAndDate(item);
                        displayTextWithEmoji(item);
                    }
                } else {
                    displayNameAndDate(item);
                    displayTextWithEmoji(item);
                }

                if (item instanceof PrivateMessage) {

                    if (item.getText().startsWith(GAME_SYSTEM)) {
                        this.setText(item.getText().substring(GAME_PREFIX.length()));
                    } else if (item.getText().startsWith(GAME_PREFIX)) {
                        this.setStyle("-fx-font-size: 12");
                        this.setText(timeLabel().getText() + item.getFrom() + ": " + item.getText().substring(GAME_PREFIX.length()));
                    }
                } else {
                    if (containsMarking(item.getText())) {
                        this.getStyleClass().add("marked_message");
                    }
                }
            }
        }

        private Label timeLabel() {
            lblTime.setText(time + ": ");
            lblTime.setStyle("-fx-font-size: 12");
            return lblTime;
        }

        public void initToolTip(S item) {
            Tooltip toolTipDate = new Tooltip();
            toolTipDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(item.getTimestamp())));
            toolTipDate.setStyle("-fx-font-size: 10");
            this.lblDate.setTooltip(toolTipDate);
            this.label.setTooltip(toolTipDate);
        }

        private String checkTime(S item) {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            if (new SimpleDateFormat("dd.MM.yyyy").format(new Date(item.getTimestamp())).equals(dateFormat.format(yesterday()))) {
                return LanguageResolver.getString("YESTERDAY") + " " + new SimpleDateFormat("HH:mm").format(new Date(item.getTimestamp()));
            } else if (new SimpleDateFormat("dd.MM.yyyy").format(new Date(item.getTimestamp())).equals(new SimpleDateFormat("dd.MM.yyyy").format(new Date()))) {
                return LanguageResolver.getString("TODAY") + " " + new SimpleDateFormat("HH:mm").format(new Date(item.getTimestamp()));
            }
            return new SimpleDateFormat("dd.MM.yyyy").format(new Date(item.getTimestamp()));
        }

        private Date yesterday() {
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            return cal.getTime();
        }

        private boolean containsMarking(String message) {
            return message.contains("@" + stageManager.getEditor().getLocalUser().getName());
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
                if (url.contains(MP3)) return true;
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
                if (url.contains(MP4) || url.contains(MP3)) {
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
            displayNameAndDate(item);
            if (!item.getText().contains(YT_WATCH) && !item.getText().contains(YT_SHORT)) {
                if (item.getText().contains(MP4) || item.getText().contains(MP3)) {
                    setUpBtnMedia(imagePlay);
                    vBox.getChildren().addAll(label, mediaView, btnHandleMedia, hyperlink);
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
            initToolTip(item);
            hyperlink.setText(item.getText());
            hyperlink.getStyleClass().add("link");
            hyperlink.setOnAction(this::openHyperLink);
        }

        private void setUpBtnMedia(Image image) {
            imgVwBtnHandleMedia.setImage(image);
            imgVwBtnHandleMedia.setFitHeight(20);
            imgVwBtnHandleMedia.setFitWidth(20);
            imgVwBtnHandleMedia.setPreserveRatio(true);
            btnHandleMedia.setGraphic(imgVwBtnHandleMedia);
            btnHandleMedia.setOnAction(this::btnHandleMediaOnClick);
        }

        private void btnHandleMediaOnClick(ActionEvent actionEvent) {
            boolean playing = mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING);
            if (!playing) {
                setUpBtnMedia(imageStop);
                mediaPlayer.play();
            } else {
                setUpBtnMedia(imagePlay);
                mediaPlayer.stop();
            }
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
            enterServerLabel.setStyle(this.label.getStyle());
            enterServerLabel.setText(LanguageResolver.getString("ENTER_SERVER"));
            Label serverIdLabel = new Label();
            serverIdLabel.setStyle(this.label.getStyle());
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
            joinServerHBox.setMaxWidth(vBox.getMaxWidth());

            displayNameAndDate(item);
            initToolTip((S) item);
            label.setText(item.getText());

            this.vBox.getChildren().addAll(joinServerHBox, label);
            setGraphic(this.vBox);
        }

        private void displayTextWithEmoji(Message item) {
            if(item.getText().contains("https://") || item.getText().contains("http://")){
                Label label = new Label(item.getText());
                label.setStyle(this.label.getStyle());
                this.vBox.getChildren().add(label);
            }
            else{
                this.emojiTextFlow.parseAndAppend(item.getText());
                this.vBox.getChildren().add(emojiTextFlow);
            }
            setGraphic(vBox);
        }

        private void displayNameAndDate(Message item){
            HBox nameAndDateHBox = new HBox();
            nameAndDateHBox.setAlignment(Pos.CENTER_LEFT);
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
            lblDate.setText(time);
            lblDate.getStyleClass().add("date");
            initToolTip((S) item);
            lblDate.getStyleClass().add("date");
            nameAndDateHBox.getChildren().addAll(name, lblDate);
            this.vBox.getChildren().add(nameAndDateHBox);
            setGraphic(vBox);
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


