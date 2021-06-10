package de.uniks.stp.wedoit.accord.client.db.dao;

import de.uniks.stp.wedoit.accord.client.db.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    @Query("SELECT u FROM ChatEntity u WHERE u.user = :userChat")
    ChatEntity findUserChat(
            @Param("userChat")
            String user
    );


}
