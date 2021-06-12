package de.uniks.stp.wedoit.accord.client.db.EntityMapper;

import de.uniks.stp.wedoit.accord.client.db.entity.ChatEntity;
import de.uniks.stp.wedoit.accord.client.db.entity.PrivateMessageEntity;
import de.uniks.stp.wedoit.accord.client.model.Chat;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.User;

import java.util.stream.Collectors;

public class ChatMapper {
    //String id, long timestamp,String text,String sender, String to
    public static ChatEntity ToEntity(Chat domain){
        return new ChatEntity(
                domain.getUser().getName());
    }



    public static Chat FromEntity(ChatEntity entity){
        return new Chat()
                .setName(entity.getName())
                .setUser(new User().setName(entity.getUser()))
                .withMessages(
                        entity.getMessages().stream().map((e) -> new PrivateMessage()
                                .setTimestamp(e.getTimestamp())
                                .setText(e.getText())
                                .setFrom(e.getSender())
                                .setTo(e.getTo())
                        ).collect(Collectors.toList()));
    }
}
