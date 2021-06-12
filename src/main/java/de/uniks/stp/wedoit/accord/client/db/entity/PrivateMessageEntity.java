package de.uniks.stp.wedoit.accord.client.db.entity;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
public class PrivateMessageEntity {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "timestamp")
    private Long timestamp;

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

    public PrivateMessageEntity(long timestamp, String text, String sender, String to,ChatEntity chat){
        this.timestamp = timestamp;
        this.text = text;
        this.sender = sender;
        this.to = to;
        this.chat = chat;
    }


    public long getId(){
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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
        chatEntity.addMessage(this);
    }

    public ChatEntity getChat() {
        return chat;
    }
}
