package de.uniks.stp.wedoit.accord.client.view;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Message;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
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
import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.MAIN_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.SERVER_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_SYSTEM;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

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
        private final VBox vBox = new VBox();
        private final Label label = new Label();
        private final Hyperlink hyperlink = new Hyperlink(), descBox = new Hyperlink();
        private final WebView webView = new WebView();
        private String time;
        EmojiTextFlowParameters parameters = new EmojiTextFlowParameters();
        EmojiTextFlow emojiTextFlow;

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
            hyperlink.getStyleClass().removeAll("link", "descBox");

            // parameters for emoji
            parameters.setEmojiScaleFactor(1D);
            parameters.setTextAlignment(TextAlignment.LEFT);
            int fontSize = stageManager.getEditor().getFontSize();
            parameters.setFont(Font.font("System", FontWeight.NORMAL, fontSize));
            parameters.setTextColor(Color.BLACK);
            emojiTextFlow = new EmojiTextFlow(parameters);


            if (!empty) {

                // set the width (-20 to eliminate overhang in ListView)
                prefWidthProperty().bind(param.widthProperty().subtract(20));
                setMinWidth(param.getWidth() - 20);
                setMaxWidth(param.getWidth() - 20);
                setAlignment(Pos.CENTER_LEFT);

                // allow wrapping
                setWrapText(true);

                time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(item.getTimestamp()));

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

                } else if (item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_ID)
                        && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_ID.length())
                        && (item.getText()).startsWith(QUOTE_PREFIX)) {

                    String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());

                    String[] messages = quoteMessage.split(QUOTE_ID);

                    parameters.setFont(Font.font("System", FontWeight.NORMAL, stageManager.getEditor().getFontSize() - 3));
                    this.emojiTextFlow.parseAndAppend(">>>" + messages[0]);
                    this.vBox.getChildren().addAll(emojiTextFlow);
                    setGraphic(vBox);
                } else if (item.getText().contains("https://ac.uniks.de/api/servers/") && item.getText().contains("/invites/")) {
                    String url = containsInviteUrl(item.getText());
                    if (url != null) {
                        setUpJoinServerView(item, url);
                    } else {
                        displayTextWithEmoji(item);
                    }
                } else {
                    displayTextWithEmoji(item);
                }
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
                Image image = new Image(url, 370, Integer.MAX_VALUE, true, false, true);
                if (!image.isError()) {
                    imageView.setImage(image);
                    imageView.setPreserveRatio(true);
                    setGraphic(vBox);
                    return true;
                }
            }
            return false;
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
                vBox.getChildren().addAll(label, imageView, hyperlink);
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

        private void displayTextWithEmoji(Message item) {
            this.label.setText("[" + time + "] " + item.getFrom() + ": ");
            this.emojiTextFlow.parseAndAppend(item.getText());
            this.vBox.getChildren().addAll(label, emojiTextFlow);
            setGraphic(vBox);
        }

    }


    private void joinButtonOnClick(String inviteLink) {
        stageManager.getEditor().getRestManager().joinServer(stageManager.getEditor().getLocalUser(), inviteLink, this);
    }

    public void handleInvitation(Server server, String responseMessage) {
        if (server != null) {
            Platform.runLater(() -> this.stageManager.initView(STAGE, LanguageResolver.getString("SERVER"), "ServerScreen", SERVER_SCREEN_CONTROLLER, true, server, null));
        } else {
            if (responseMessage.equals("MainScreen")) {
                Platform.runLater(() -> this.stageManager.initView(STAGE, LanguageResolver.getString("MAIN"), "MainScreen", MAIN_SCREEN_CONTROLLER, true, null, null));
            }
        }
    }

}


