package de.uniks.stp.wedoit.accord.client.db.dao;

import de.uniks.stp.wedoit.accord.client.db.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ChatRepository extends JpaRepository<ChatEntity, String> {


    @Query("SELECT u FROM ChatEntity u WHERE u.user = :userChat ")
    ChatEntity findUserChat(
            @Param("userChat")
            String user
    );


}
