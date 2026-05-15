package com.splitmate.backend.repository;

import com.splitmate.backend.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, String> {
    List<ExpenseSplit> findByExpenseId(String expenseId);

    List<ExpenseSplit> findByUserId(String userId);
}
