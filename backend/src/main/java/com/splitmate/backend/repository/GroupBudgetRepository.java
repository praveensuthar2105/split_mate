package com.splitmate.backend.repository;

import com.splitmate.backend.model.GroupBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupBudgetRepository extends JpaRepository<GroupBudget, String> {
    Optional<GroupBudget> findByGroupId(String groupId);
}