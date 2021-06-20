package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.SystemTrayController;
import de.uniks.stp.wedoit.accord.client.db.SqliteDB;
import de.uniks.stp.wedoit.accord.client.model.*;
import javafx.application.Platform;

import javax.json.JsonObject;
import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.QUOTE_PREFIX;

public class MessageManager {

    private final Editor editor;

    public MessageManager(Editor editor){
        this.editor = editor;
    }

    /**
     * add message to privateChat of corresponding user
     *
     * @param message to add to the model
     */
    public void addNewPrivateMessage(PrivateMessage message) {
        
        if(message.getText().startsWith(GAME_PREFIX) && handleGameMessages(message)) return;
        
        if (message.getFrom().equals(editor.getLocalUser().getName())) {
            editor.getUser(message.getTo()).getPrivateChat().withMessages(message);
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

    private boolean handleGameMessages(PrivateMessage message){
        //game messages
        if(message.getText().equals(GAME_INVITE)){
            if(message.getTo().equals(editor.getLocalUser().getName()))
                editor.getLocalUser().withGameInvites(editor.getUser(message.getFrom()));
            else
                editor.getLocalUser().withGameRequests(editor.getUser(message.getTo()));
        }

        if(message.getText().equals(GAME_ACCEPTS)){
            if(!editor.getStageManager().getGameStage().isShowing() || editor.getStageManager().getGameStage().getTitle().equals("Result")) {
                JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(message.getTo().equals(editor.getLocalUser().getName()) ? message.getFrom(): message.getTo(), GAME_START);
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
                return true;

            }else{
                System.out.println("this is: " + editor.getLocalUser().getName());
                System.out.println( editor.getStageManager().getGameStage().getTitle());
                System.out.println(!editor.getStageManager().getGameStage().isShowing());
                System.out.println(!editor.getStageManager().getGameStage().isShowing() || editor.getStageManager().getGameStage().getTitle().equals("Result"));
                JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(message.getFrom().equals(editor.getLocalUser().getName()) ? message.getTo(): message.getFrom(), GAME_INGAME);
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
                return true;

            }
        }else if(message.getText().equals(GAME_START)  && (editor.getLocalUser().getGameInvites().contains(editor.getUser(message.getTo())) || editor.getLocalUser().getGameRequests().contains(editor.getUser(message.getFrom())))) {
            //Start game
            editor.getLocalUser().withoutGameInvites(editor.getUser(message.getTo()));
            editor.getLocalUser().withoutGameRequests(editor.getUser(message.getFrom()));

            Platform.runLater(() -> {
                if (message.getFrom().equals(editor.getLocalUser().getName()))
                    editor.getStageManager().showGameScreen(editor.getUser(message.getTo()));
                else
                    editor.getStageManager().showGameScreen(editor.getUser(message.getFrom()));
            });

        }else if(message.getText().equals(GAME_CLOSE) && editor.getStageManager().getGameStage().isShowing()){
            Platform.runLater(() -> editor.getStageManager().showGameResultScreen(editor.getUser(message.getFrom()),null));
        }

        if (message.getText().startsWith(GAME_PREFIX) && (message.getText().endsWith(GAME_ROCK) || message.getText().endsWith(GAME_PAPER) || message.getText().endsWith(GAME_SCISSORS))) {
            if (!message.getFrom().equals(editor.getLocalUser().getName()))
                editor.getUser(message.getFrom()).setGameMove(message.getText().substring(GAME_PREFIX.length()));
            return true;
        }
        return false;
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
     * formats a message with the correct date in the format
     * <p>
     * [" + dd/MM/yyyy HH:mm:ss + "] " + FROM + ": " + MESSAGE
     * @param message message which should formatted
     * @return the formatted message as string
     */
    public String getMessageFormatted(PrivateMessage message) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(message.getTimestamp()));

        return ("[" + time + "] " + message.getFrom() + ": " + message.getText());
    }

    /**
     * formats a message with the correct date in the format
     * <p>
     * [" + dd/MM/yyyy HH:mm:ss + "] " + FROM + ": " + MESSAGE
     * @param message message which should formatted
     * @return the formatted message as string
     */
    public String getMessageFormatted(Message message) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(message.getTimestamp()));

        return ("[" + time + "] " + message.getFrom() + ": " + message.getText());
    }

    /**
     * creates a clean message from a quote
     */
    public String cleanMessage(PrivateMessage item) {
        if (isQuote(item)) {
            String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());
            String[] messages = quoteMessage.split(QUOTE_ID);
            return messages[0];
        } else return item.getText();
    }

    /**
     * checks whether a message is a quote
     * @param item item as message
     * @return boolean whether a item is a quote
     */
    public boolean isQuote(PrivateMessage item) {
        return item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_ID)
                && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_ID.length())
                && (item.getText()).startsWith(QUOTE_PREFIX);
    }

    /**
     * checks whether a message is a quote
     * @param item item as message
     * @return boolean whether a item is a quote
     */
    public boolean isQuote(Message item) {
        return item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_ID)
                && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_ID.length())
                && (item.getText()).startsWith(QUOTE_PREFIX);
    }
}
