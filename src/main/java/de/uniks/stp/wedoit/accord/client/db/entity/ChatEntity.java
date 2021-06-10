package de.uniks.stp.wedoit.accord.client.db.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
public class ChatEntity {



    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "user")
    private String user;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = PrivateMessageEntity.class)
    private List<PrivateMessageEntity> messages;


    public ChatEntity(){
        super();
    }

    public ChatEntity(String user){
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public List<PrivateMessageEntity> getMessages() {
        if(this.messages == null) this.messages = new ArrayList<>();
        return messages;
    }

    public void setMessages(List<PrivateMessageEntity> messages) {
        this.messages = messages;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
