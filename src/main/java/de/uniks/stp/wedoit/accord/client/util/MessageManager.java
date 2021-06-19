package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.SystemTrayController;
import de.uniks.stp.wedoit.accord.client.model.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.Game.PREFIX;
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
        if (message.getText().equals(GAMEINVITE)) {
            if (message.getFrom().equals(editor.getLocalUser().getName()))
                editor.getLocalUser().withGameRequests(editor.getUser(message.getTo()));
            else editor.getLocalUser().withGameInvites(editor.getUser(message.getFrom()));
            message.setText(message.getText().substring(PREFIX.length()));
        }
        if (message.getText().startsWith(PREFIX) && (message.getText().endsWith(ROCK) || message.getText().endsWith(PAPER) || message.getText().endsWith(SCISSORS))) {
            if (!message.getFrom().equals(editor.getLocalUser().getName()))
                editor.getUser(message.getFrom()).setGameMove(message.getText().substring(PREFIX.length()));

        } else {
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
                privateChat.withMessages(message);
                user.setChatRead(false);
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
