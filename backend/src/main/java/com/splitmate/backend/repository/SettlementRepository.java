package com.splitmate.backend.repository;

import com.splitmate.backend.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, String> {
    List<Settlement> findByGroupId(String groupId);

    List<Settlement> findByFromUserOrToUser(String fromUser, String toUser);
}
