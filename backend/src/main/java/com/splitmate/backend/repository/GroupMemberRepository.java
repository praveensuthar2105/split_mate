package com.splitmate.backend.repository;

import com.splitmate.backend.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {
    List<GroupMember> findByGroupId(String groupId);

    List<GroupMember> findByUserId(String userId);

    boolean existsByGroupIdAndUserId(String groupId, String userId);
}
