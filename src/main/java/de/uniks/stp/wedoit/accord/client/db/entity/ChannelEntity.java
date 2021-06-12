package de.uniks.stp.wedoit.accord.client.db.entity;

import javax.persistence.Id;
import java.util.List;

public class ChannelEntity {

    @Id
    private String id;

    private String name;

    private String type;

    private boolean privileged;

    private boolean read;


    //name of category
    String category;

    //list of user names
    List<String> members;

    List<MessageEntity> messages;
}
