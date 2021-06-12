package de.uniks.stp.wedoit.accord.client.db.EntityMapper;

import de.uniks.stp.wedoit.accord.client.db.entity.ChatEntity;
import de.uniks.stp.wedoit.accord.client.db.entity.PrivateMessageEntity;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;

public class PrivateMessageMapper {

    //String id_s, long timestamp, String text, String sender, String to,ChatEntity chat
    public static PrivateMessageEntity toEntity(PrivateMessage domain){
        return new PrivateMessageEntity(
                domain.getTimestamp(),
                domain.getText(),
                domain.getFrom(),
                domain.getTo(),
                new ChatEntity(domain.getTo())
        );
    }
}
//    public static ChatEntity ToEntity(Chat domain){
//        return new ChatEntity(
//                domain.getName());
//    }