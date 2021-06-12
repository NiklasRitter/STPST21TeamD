package de.uniks.stp.wedoit.accord.client.db.entity;



import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CHATS")
public class ChatEntity {

    @Id
    @Column(name = "user")
    private String user;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = PrivateMessageEntity.class)
    private List<PrivateMessageEntity> messages;


    public ChatEntity(){
        super();
    }

    public ChatEntity(String user){
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public List<PrivateMessageEntity> getMessages() {
        if(this.messages == null) this.messages = new ArrayList<>();
        return messages;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setMessages(List<PrivateMessageEntity> messages) {
        this.messages = messages;
    }

    public void setUser(String user) {
        this.user = user;
    }


    public void addMessage(PrivateMessageEntity message){
        getMessages().add(message);
        message.setChat(this);
    }

}
