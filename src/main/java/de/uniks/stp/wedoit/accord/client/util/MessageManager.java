package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.GameScreenController;
import de.uniks.stp.wedoit.accord.client.controller.SystemTrayController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import javafx.application.Platform;

import javax.json.JsonObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.GAME_RESULT_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.GAME_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.GAME_STAGE;

public class MessageManager {

    private final Editor editor;

    public MessageManager(Editor editor) {
        this.editor = editor;
    }

    /**
     * add message to privateChat of corresponding user
     *
     * @param message to add to the model
     */
    public void addNewPrivateMessage(PrivateMessage message) {

        if (message.getText().startsWith(GAME_PREFIX) && handleGameMessages(message)) return;

        if (message.getFrom().equals(editor.getLocalUser().getName())) {
            User user = editor.getUser(message.getTo());
            user.getPrivateChat().withMessages(message);
        } else {
            SystemTrayController systemTrayController = editor.getStageManager().getSystemTrayController();
            if (systemTrayController != null) {
                systemTrayController.displayPrivateMessageNotification(message);
            }
            User user = editor.getUser(message.getFrom());
            Chat privateChat = user.getPrivateChat();
            if (privateChat == null) {
                privateChat = new Chat().setName(user.getName()).setUser(user);
                user.setPrivateChat(privateChat);
            }
            message.setChat(privateChat);
            privateChat.withMessages(message);
            user.setChatRead(false);
            editor.updateUserChatRead(user);
        }
        editor.savePrivateMessage(message);
    }

    /**
     * @param message private message expected to have GAME_PREFIX as prefix
     * @return true if message should not be displayed in chat else false to display message
     */
    private boolean handleGameMessages(PrivateMessage message) {

        if(GAME_NOT_SUPPORTED.stream().anyMatch((e) -> message.getText().startsWith(e))) return true;

        //Game invites come from opponents
        //Game requests comes from localUser to opponent
        if (message.getText().equals(GAME_INVITE)) {
            if (message.getTo().equals(editor.getLocalUser().getName())) { //incoming !play :handshake:
                handleIncomingGameInvite(message.getFrom());
            } else { //outgoing !play :handshake:
                handleOutGoingGameInvite(message.getTo());
            }
        }
        if (message.getText().equals(GAME_REVENGE) && editor.getLocalUser().isInGame()) {
            if (message.getTo().equals(editor.getLocalUser().getName())) { //incoming !revenge
                handleIncomingGameInvite(message.getFrom());
            } else { //outgoing !revenge
                handleOutGoingGameInvite(message.getTo());
            }
        }

        if (message.getText().equals(GAME_CLOSE)) {
            handleQuitGame(message);
        }

        if (message.getText().startsWith(GAME_PREFIX) && (message.getText().endsWith(GAME_ROCK) || message.getText().endsWith(GAME_PAPER) || message.getText().endsWith(GAME_SCISSORS))) {
            if (!message.getFrom().equals(editor.getLocalUser().getName()))
                editor.getUser(message.getFrom()).setGameMove(message.getText().substring(GAME_PREFIX.length() + GAME_CHOOSE_MOVE.length()));
            return true;
        }
        return false;
    }

    private void handleQuitGame(PrivateMessage message) {
        LocalUser localUser = editor.getLocalUser();
        User opponent;

        if (localUser.getName().equals(message.getFrom())) { // outgoing quit from user --> delete game request for this opponent
            opponent = editor.getUser(message.getTo());
            localUser.withoutGameRequests(opponent);
        } else { // incoming quit from opponent --> delete invite from this opponent
            opponent = editor.getUser(message.getFrom());
            localUser.withoutGameInvites(opponent);

            // checks if quit comes from current (inGame) opponent (if yes --> leave game since opponent quit)
            GameScreenController controller = (GameScreenController) editor.getStageManager().getControllerMap().get(GAME_SCREEN_CONTROLLER);
            if (controller != null) {
                User inGameOpponent = controller.getOpponent();
                if (inGameOpponent.getName().equals(opponent.getName())) {
                    if (localUser.isInGame() && editor.getStageManager().getStage(StageEnum.GAME_STAGE).isShowing()) {
                        Platform.runLater(() -> editor.getStageManager().initView(ControllerEnum.GAME_SCREEN_RESULT, opponent, null));
                    }
                }
            }
        }
    }

    private void handleOutGoingGameInvite(String to) {
        LocalUser localUser = editor.getLocalUser();
        User opponent = editor.getUser(to);

        if (localUser.getGameInvites().contains(opponent)) {
            startGame(opponent);
            return;
        }
        if (!localUser.getGameRequests().contains(opponent)) {
            localUser.withGameRequests(opponent);
        }
    }

    private void startGame(User opponent) {
        clearAllGameInvitesAndRequests(opponent);
        editor.getLocalUser().setInGame(true);
        Platform.runLater(() -> editor.getStageManager().initView(ControllerEnum.GAME_SCREEN_INGAME, opponent, null));
    }

    private void handleIncomingGameInvite(String from) {
        LocalUser localUser = editor.getLocalUser();
        User opponent = editor.getUser(from);

        if (localUser.getGameRequests().contains(opponent)) { // the gameInvite is an answer to our gameInvite --> start Game
            startGame(opponent);
            return;
        }
        if (!localUser.getGameInvites().contains(opponent)) { //first invite from this player
            localUser.withGameInvites(opponent);
        }
    }

    private void clearAllGameInvitesAndRequests(User opponent) {
        // send a !quit to every user
        LocalUser localUser = editor.getLocalUser();
        localUser.withoutGameInvites(opponent);
        localUser.withoutGameRequests(opponent);
        if (localUser.getGameInvites() != null) {
            for (int i = 0; i < localUser.getGameInvites().size(); i++) {
                User user = localUser.getGameInvites().get(i);
                JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(user.getName(), GAME_CLOSE);
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
                localUser.withoutGameInvites(user);
            }
        }
        if (localUser.getGameRequests() != null) {
            for (int i = 0; i < localUser.getGameRequests().size(); i++) {
                User user = localUser.getGameRequests().get(i);
                JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(user.getName(), GAME_CLOSE);
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
                localUser.withoutGameRequests(user);
            }
        }
    }

    /**
     * add message to channel chat
     *
     * @param message to add to the model
     */
    public void addNewChannelMessage(Message message) {
        message.getChannel().withMessages(message);
    }

    /**
     * adds messages to a channel
     */
    public void updateChannelMessages(Channel channel, List<Message> messages) {
        List<Message> channelMessages = channel.getMessages();
        for (Message message : messages) {
            boolean msgExists = false;
            for (Message channelMessage : channelMessages) {
                if (channelMessage.getId().equals(message.getId())) {
                    msgExists = true;
                    break;
                }
            }
            if (!msgExists) {
                channel.withMessages(message);
            }
        }
    }


    /**
     * updates message in the data model
     *
     * @param channel in which the message should be updated
     * @param message to update
     */
    public void updateMessage(Channel channel, Message message) {
        for (Message channelMessage : channel.getMessages()) {
            if (channelMessage.getId().equals(message.getId())) {
                channelMessage.setText(message.getText());
                return;
            }
        }
    }

    /**
     * deletes the message with given id
     *
     * @param channel           channel of the message
     * @param messageToDeleteId id of the message to delete
     */
    public void deleteMessage(Channel channel, String messageToDeleteId) {
        Message foundMessage = null;
        for (Message message : channel.getMessages()) {
            if (message.getId().equals(messageToDeleteId)) {
                foundMessage = message;
                break;
            }
        }
        if (foundMessage != null) {
            channel.withoutMessages(foundMessage);
        }
    }

    /**
     * formats a message with the correct date in the format
     * <p>
     * [" + dd/MM/yyyy HH:mm:ss + "] " + FROM + ": " + MESSAGE
     *
     * @param message message which should formatted
     * @return the formatted message as string
     */
    public String getMessageFormatted(PrivateMessage message) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(message.getTimestamp()));
        if (message.getText().startsWith(GAME_PREFIX))
            message.setText(message.getText().substring(GAME_PREFIX.length()));
        return ("[" + time + "] " + message.getFrom() + ": " + message.getText());
    }

    /**
     * formats a message with the correct date in the format
     * <p>
     * [" + dd/MM/yyyy HH:mm:ss + "] " + FROM + ": " + MESSAGE
     *
     * @param message message which should formatted
     * @return the formatted message as string
     */
    public String getMessageFormatted(Message message, String text) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(message.getTimestamp()));

        return ("[" + time + "] " + message.getFrom() + ": " + text);
    }

    /**
     * creates a clean quote from a quote
     */
    /*public String cleanQuote(PrivateMessage item) {
        if (isQuote(item)) {
            String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());
            String[] messages = quoteMessage.split(QUOTE_MESSAGE);
            if (messages.length != 2) {
                return item.getText();
            }
            return messages[0];
        } else return item.getText();
    }*/

    /**
     * creates a clean quote from a quote
     */
    public String cleanQuote(Message item) {
        if (isQuote(item)) {
            String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());
            String[] messages = quoteMessage.split(QUOTE_MESSAGE);
            if (messages.length != 2) {
                return item.getText();
            }
            return messages[0];
        } else return item.getText();
    }

    /**
     * creates a clean message from a quote
     */
    public String cleanQuoteMessage(Message item) {
        if (isQuote(item)) {
            String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());
            String[] messages = quoteMessage.split(QUOTE_MESSAGE);
            if (messages.length != 2) {
                return item.getText();
            }
            return messages[1];
        } else return item.getText();
    }

    /**
     * checks whether a message is a quote
     *
     * @param item item as message
     * @return boolean whether a item is a quote
     */
    /*public boolean isQuote(PrivateMessage item) {
        return item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_MESSAGE)
                && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_MESSAGE.length())
                && (item.getText()).startsWith(QUOTE_PREFIX);
    }*/

    /**
     * checks whether a message is a quote
     *
     * @param item item as message
     * @return boolean whether a item is a quote
     */
    public boolean isQuote(Message item) {
        return item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_MESSAGE)
                && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_MESSAGE.length())
                && (item.getText()).startsWith(QUOTE_PREFIX);
    }
}
