package com.splitmate.backend.repository;

import com.splitmate.backend.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    List<Group> findByCreatedBy(String createdBy);

    List<Group> findByArchivedFalse();
}
