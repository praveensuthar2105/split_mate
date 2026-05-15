package com.splitmate.backend.service;

import com.splitmate.backend.algorithm.DebtMinimizer;
import com.splitmate.backend.dto.SettlementTransaction;
import com.splitmate.backend.model.Expense;
import com.splitmate.backend.model.ExpenseSplit;
import com.splitmate.backend.model.GroupMember;
import com.splitmate.backend.repository.ExpenseRepository;
import com.splitmate.backend.repository.ExpenseSplitRepository;
import com.splitmate.backend.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final DebtMinimizer debtMinimizer;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupMemberRepository groupMemberRepository;

    public List<SettlementTransaction> getMinimizedSettlements(String groupId) {
        return debtMinimizer.minimize(calculateNetBalances(groupId));
    }

    public Map<String, BigDecimal> calculateNetBalances(String groupId) {
        Map<String, BigDecimal> netBalances = new LinkedHashMap<>();
        groupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(GroupMember::getUserId)
                .forEach(userId -> netBalances.put(userId, BigDecimal.ZERO));

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        for (Expense expense : expenses) {
            netBalances.putIfAbsent(expense.getPaidBy(), BigDecimal.ZERO);
            for (ExpenseSplit split : expenseSplitRepository.findByExpenseId(expense.getId())) {
                netBalances.putIfAbsent(split.getUserId(), BigDecimal.ZERO);
                if (!split.getUserId().equals(expense.getPaidBy())) {
                    netBalances.merge(split.getUserId(), split.getShareAmount().negate(), BigDecimal::add);
                    netBalances.merge(expense.getPaidBy(), split.getShareAmount(), BigDecimal::add);
                }
            }
        }

        return netBalances;
    }
}
