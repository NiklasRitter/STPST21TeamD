package de.uniks.stp.wedoit.accord.client.db.entity;

import javax.persistence.*;

@Entity
public class PrivateMessageEntity {

    @Id
    @Column(name = "id")
    String id;

    @Column(name = "timestamp")
    long timestamp;

    @Column(name = "text")
    private String text;

    @Column(name = "sender")
    private String sender;

    @Column(name = "to")
    private String to;

    @ManyToOne
    @JoinColumn(name = "chat_history")
    private ChatEntity chat;

    public PrivateMessageEntity() {
        super();
    }

    public PrivateMessageEntity(String id, long timestamp,String text,String sender, String to){
        this.id = id;
        this.timestamp = timestamp;
        this.text = text;
        this.sender = sender;
        this.to = to;
    }



    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public String getTo() {
        return to;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setChat(ChatEntity chatEntity) {
        this.chat = chatEntity;
    }

    public ChatEntity getChat() {
        return chat;
    }
}
