package de.uniks.stp.wedoit.accord.client.db.dao;

import de.uniks.stp.wedoit.accord.client.db.entity.PrivateMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PrivateMessageRepository extends JpaRepository<PrivateMessageEntity,Long> {


}
