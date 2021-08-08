package de.uniks.stp.wedoit.accord.client.view;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.ServerScreenController;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.CategoryTreeViewController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.util.EmojiTextFlowParameterHelper;
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
import java.util.*;

import static de.uniks.stp.wedoit.accord.client.constants.ChatMedia.*;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_SYSTEM;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.AUDIO;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.TEXT;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.SLASH;

public class MessageCellFactory<T extends Message> implements Callback<ListView<T>, ListCell<T>> {

    private final Controller controller;
    StageManager stageManager;

    public MessageCellFactory(StageManager stageManager, Controller controller) {
        this.stageManager = stageManager;
        this.controller = controller;
    }

    @Override
    public ListCell<T> call(ListView<T> param) {
        return new MessageCell<>(param);
    }

    private void joinButtonOnClick(String inviteLink) {
        stageManager.getEditor().getRestManager().joinServer(stageManager.getEditor().getLocalUser(), inviteLink, this);
    }

    public void handleInvitation(Server server, String responseMessage) {
        if (server != null) {
            Platform.runLater(() -> this.stageManager.initView(ControllerEnum.SERVER_SCREEN, server, null));
        } else {
            if (responseMessage.equals("MainScreen")) {
                Platform.runLater(() -> this.stageManager.initView(ControllerEnum.PRIVATE_CHAT_SCREEN, null, null));
            }
        }
    }

    private class MessageCell<S extends Message> extends ListCell<S> {

        private final ListView<S> param;
        private final ImageView imageView = new ImageView();
        private final ImageView imgVwBtnHandleMedia = new ImageView();
        private final Image imagePlay = new Image(Objects.requireNonNull(MessageCellFactory.class.getResourceAsStream("images/play.png")));
        private final Image imageStop = new Image(Objects.requireNonNull(MessageCellFactory.class.getResourceAsStream("images/stop.png")));
        private final Button btnHandleMedia = new Button();
        private final Label lblDate = new Label();
        private final MediaView mediaView = new MediaView();
        private final VBox vBox = new VBox();
        private final Label label = new Label();
        private final Label lblTime = new Label();
        private final Hyperlink hyperlink = new Hyperlink(), descBox = new Hyperlink();
        private final WebView webView = new WebView();
        private MediaPlayer mediaPlayer;
        private String time;
        private EmojiTextFlowParameters parameters;
        private EmojiTextFlowParameters parametersQuote;
        private EmojiTextFlowParameters referenceParameters;
        private EmojiTextFlow emojiTextFlow;
        private EmojiTextFlow quoteTextFlow;
        private final Button spoilerButton = new Button("Spoiler");


        private MessageCell(ListView<S> param) {
            this.param = param;
        }

        /**
         * determinants how Message cell is constructed
         *
         * @param item  of PrivateMessage or Message
         * @param empty if cell is empty
         */
        @Override
        protected void updateItem(S item, boolean empty) {
            super.updateItem(item, empty);
            setItem(item);
            spoilerButton.setOnAction(null);
            this.setText(null);
            this.getStyleClass().removeAll("font_size", "marked_message", "reference");
            this.setGraphic(null);
            this.vBox.getChildren().clear();
            webView.getEngine().load(null);
            descBox.setText(null);
            hyperlink.setOnAction(null);
            hyperlink.getStyleClass().removeAll("link", "descBox");

            // parameters for emoji
            this.parametersQuote =
                    new EmojiTextFlowParameterHelper(stageManager.getEditor().getAccordClient().getOptions().getChatFontSize() - 3).createParameters();
            this.parameters =
                    new EmojiTextFlowParameterHelper(stageManager.getEditor().getAccordClient().getOptions().getChatFontSize()).createParameters();
            this.referenceParameters =
                    new EmojiTextFlowParameterHelper(stageManager.getEditor().getAccordClient().getOptions().getChatFontSize()).createParameters();
            if (stageManager.getPrefManager().loadDarkMode()) {
                this.parameters.setTextColor(Color.valueOf("#ADD8e6"));
                this.parametersQuote.setTextColor(Color.valueOf("#ADD8e6"));
                this.referenceParameters.setTextColor(Color.valueOf("#ffffff"));
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

                //remove game prefix
                if (item.getText().startsWith(GAME_PREFIX))
                    item.setText(item.getText().substring(GAME_PREFIX.length()));

                //eval correct time format
                time = checkTime(item);

                if (item instanceof PrivateMessage) {
                    //private message handling
                    if (item.getText().startsWith(GAME_SYSTEM)) {
                        this.setText(item.getText().substring(GAME_PREFIX.length()));
                        return;
                    } else if (item.getText().startsWith(GAME_PREFIX)) {
                        this.setText("[" + time + "] " + item.getFrom() + ": " + item.getText().substring(GAME_PREFIX.length()));
                        return;
                    }
                } else {
                    //marking in server chats
                    if (containsMarking(item.getText())) {
                        this.getStyleClass().add("marked_message");
                    }
                }

                if (item.getText().startsWith("%") && item.getText().endsWith("%")) {
                    //spoiler function
                    displayNameAndDate(item);
                    displaySpoilerButton(item);

                } else if (setImgGraphic(item.getText()) && !item.getText().contains(QUOTE_PREFIX)) {
                    //media view
                    setUpMedia(item);

                } else if (item.getId() != null && item.getId().equals("idLoadMore")) {
                    //load more option if more than 50 messages in chat
                    setAlignment(Pos.CENTER);
                    this.setText(item.getText());

                } else if (item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_MESSAGE)
                        && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_MESSAGE.length())
                        && (item.getText()).startsWith(QUOTE_PREFIX)) {
                    //handle quote formatting
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
                    //invitation links for other server
                    String url = containsInviteUrl(item.getText());
                    if (url != null) {
                        setUpJoinServerView(item, url);
                    } else {
                        this.setStyle("-fx-font-size: 12");
                        displayNameAndDate(item);
                        displayTextWithEmoji(item);
                    }
                } else if (!(item instanceof PrivateMessage)) {
                    ArrayList<Channel> referencedChannels = getReferences(item, item.getText());

                    if (!referencedChannels.isEmpty()) {
                        HBox hBox = new HBox();
                        displayNameAndDate(item);
                        setUpReferenceInMessage(item, referencedChannels, hBox, item.getText());

                        this.vBox.getChildren().add(hBox);
                        setGraphic(this.vBox);

                    } else {
                        displayNameAndDate(item);
                        displayTextWithEmoji(item);
                    }



                }
                else {
                    //normal message possibly with emoji
                    displayNameAndDate(item);
                    displayTextWithEmoji(item);
                }

                if(item.getText().startsWith(MESSAGE_LINK + SLASH) && item.getText().split("/").length == 6) {
                    Hyperlink hyperlink = new Hyperlink(item.getText());
                    hyperlink.setId("messageLink");
                    vBox.getChildren().clear();
                    displayNameAndDate(item);
                    vBox.getChildren().add(hyperlink);
                    hyperlink.getStyleClass().add("link");
                    hyperlink.setOnAction(event -> openMessage(item));

                }
            }
        }

        private void openMessage(Message message) {
            String[] parsedReferenceMessage = stageManager.getEditor().parseReferenceMessage(message.getText());
            Server server = null;
            for (Server serverIndex: stageManager.getModel().getLocalUser().getServers()) {
                if (serverIndex.getId().equals(parsedReferenceMessage[1])){
                    server = serverIndex;
                }
            }
            if (server != null) {
                server.setReferenceMessage(message.getText());
                Server finalServer = server;
                Platform.runLater(() -> stageManager.initView(ControllerEnum.SERVER_SCREEN, finalServer, null));

            }
        }

        private ArrayList<Channel> getReferences(Message item, String text) {
            ArrayList<Channel> channels = new ArrayList<>();
            Server server = item.getChannel().getCategory().getServer();

            for (Category category : server.getCategories()) {
                for (Channel channel : category.getChannels()) {
                    if (text.contains("#" + channel.getName())) {
                        channels.add(channel);
                    }
                }
            }
            return channels;
        }

        public void initToolTip(S item) {
            Tooltip toolTipDate = new Tooltip();
            toolTipDate.setText(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(item.getTimestamp())));
            toolTipDate.setStyle("-fx-font-size: 10");
            this.lblDate.setTooltip(toolTipDate);
            this.label.setTooltip(toolTipDate);
        }

        /**
         * checks time and determines if time was today, yesterday
         *
         * @param item message item
         * @return String with correct description of time
         */
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

        /**
         * @param message to be checked for @localUserName
         * @return True if message contains localUserName else false
         */
        private boolean containsMarking(String message) {
            return message.contains("@" + stageManager.getEditor().getLocalUser().getName());
        }

        /**
         * @param text to be checked for server invite
         * @return invite url if is valid url else null
         */
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

        /**
         * check for a valid url in general
         *
         * @param url string to be checked
         * @return true if is valid else false
         */
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

        /**
         * @param url a string that might be a url
         * @return true image could be set
         */
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

        /**
         * @param url to be loaded into a media view
         */
        private void setUpMediaView(String url) {
            Media media = new Media(url);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setFitHeight(400);
            mediaView.setFitWidth(270);
            mediaView.setPreserveRatio(true);
            mediaView.setMediaPlayer(mediaPlayer);
        }

        /**
         * @param url to be set up in webView (for youtube videos)
         */
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

        /**
         * opens standard browser
         *
         * @param url that the browser opens
         */
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
            label.setWrapText(true);

            this.vBox.getChildren().addAll(joinServerHBox, label);
            setGraphic(this.vBox);
        }

        private void setUpReferenceInMessage(Message item, ArrayList<Channel> referencedChannels, HBox message, String currentText) {
            String current = currentText;
            boolean duplications = false;

            for (Channel channel : referencedChannels) {
                int start = current.indexOf("#" + channel.getName());
                if (start == -1) {
                    continue;
                }

                EmojiTextFlow emojiTextFlow = new EmojiTextFlow(this.parameters);
                emojiTextFlow.parseAndAppend(current.substring(0, start));
                message.getChildren().add(emojiTextFlow);

                EmojiTextFlow emojiTextFlowClickable = new EmojiTextFlow(this.referenceParameters);
                emojiTextFlowClickable.parseAndAppend("#" + channel.getName());
                emojiTextFlowClickable.setOnMousePressed(event -> referenceButtonOnClick(channel));
                emojiTextFlowClickable.getStyleClass().add("reference");
                emojiTextFlowClickable.setId("reference");
                message.getChildren().add(emojiTextFlowClickable);

                current = current.substring(start + channel.getName().length() + 1);

                if (current.contains("#" + channel.getName())) {
                    duplications = true;
                    break;
                }
            }

            if (duplications) {
                ArrayList<Channel> references = getReferences(item, current);
                setUpReferenceInMessage(item, references, message, current);
            } else if (!current.isEmpty()) {
                EmojiTextFlow emojiTextFlow = new EmojiTextFlow(this.parameters);
                emojiTextFlow.parseAndAppend(current);
                message.getChildren().add(emojiTextFlow);
            }
        }

        private void displayTextWithEmoji(Message item) {
            this.emojiTextFlow.parseAndAppend(item.getText());
            this.vBox.getChildren().add(emojiTextFlow);
            setGraphic(vBox);
        }

        /**
         * content of message will be loaded into the list when the button is pressed
         *
         * @param item to be loaded
         */
        private void displaySpoilerButton(S item) {
            spoilerButton.getStyleClass().add("styleButton");
            vBox.getChildren().add(spoilerButton);
            spoilerButton.setOnAction((e) -> {
                vBox.getChildren().remove(spoilerButton);
                item.setText(item.getText().substring(1, item.getText().length() - 1));
                displayTextWithEmoji(item);
                spoilerButton.setOnAction(null);
            });
            setGraphic(vBox);

        }

        private void displayNameAndDate(Message item) {
            HBox nameAndDateHBox = new HBox();
            nameAndDateHBox.setAlignment(Pos.CENTER_LEFT);
            Label name = new Label(item.getFrom() + " ");

            int nameLength = item.getFrom().length();

            //selects css class called "color0" to "color5"
            name.getStyleClass().add("color" + nameLength % 5);

            lblDate.setText(time);
            lblDate.getStyleClass().add("date");
            initToolTip((S) item);
            lblDate.getStyleClass().add("date");
            nameAndDateHBox.getChildren().addAll(name, lblDate);
            this.vBox.getChildren().add(nameAndDateHBox);
            setGraphic(vBox);
        }
    }


    private void referenceButtonOnClick(Channel channel) {
        ServerScreenController serverScreenController = (ServerScreenController) controller;
        CategoryTreeViewController categoryTreeViewController = serverScreenController.getCategoryTreeViewController();

        TreeView<Object> tvServerChannels = categoryTreeViewController.getTvServerChannels();
        TreeItem<Object> treeItem = tvServerChannels.getRoot();

        if (channel.getType().equals(AUDIO)) {
            categoryTreeViewController.handleAudioDoubleClicked(channel);
        } else if (channel.getType().equals(TEXT)) {
            serverScreenController.getServerChatController().initChannelChat(channel);
            serverScreenController.refreshLvUsers(channel);

            treeItem = getItem(treeItem, channel.getName());

            if (treeItem != null) {
                tvServerChannels.getSelectionModel().select(treeItem);
            }
        }
    }

    private TreeItem<Object> getItem(TreeItem<Object> item, String name) {

        if (item != null && (item.getValue() instanceof Channel) && ((Channel) item.getValue()).getName().equals(name))
            return item;

        for (TreeItem<Object> child : item.getChildren()) {
            TreeItem<Object> treeItem = getItem(child, name);
            if (treeItem != null)
                return treeItem;
        }
        return null;
    }

}


