package com.splitmate.backend.repository;

import com.splitmate.backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    List<Expense> findByGroupId(String groupId);

    List<Expense> findByPaidBy(String userId);
}
