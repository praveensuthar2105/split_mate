package com.splitmate.backend.service;

import com.splitmate.backend.algorithm.DebtMinimizer;
import com.splitmate.backend.dto.SettlementTransaction;
import com.splitmate.backend.model.Expense;
import com.splitmate.backend.model.ExpenseSplit;
import com.splitmate.backend.model.GroupMember;
import com.splitmate.backend.model.Settlement;
import com.splitmate.backend.repository.ExpenseRepository;
import com.splitmate.backend.repository.ExpenseSplitRepository;
import com.splitmate.backend.repository.GroupMemberRepository;
import com.splitmate.backend.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final SettlementRepository settlementRepository;

    public List<SettlementTransaction> getMinimizedSettlements(String groupId) {
        return debtMinimizer.minimize(calculateNetBalances(groupId))
                .stream()
                .peek(txn -> txn.setId(buildSettlementKey(
                        groupId,
                        txn.getFromUserId(),
                        txn.getToUserId(),
                        txn.getAmount())))
                .toList();
    }

    public Settlement markSettled(String groupId, String fromUserId, String toUserId, BigDecimal amount) {
        return settlementRepository.findByGroupId(groupId)
                .stream()
                .filter(settlement -> settlement.isSettled()
                        && settlement.getFromUser().equals(fromUserId)
                        && settlement.getToUser().equals(toUserId)
                        && settlement.getAmount().compareTo(amount) == 0)
                .findFirst()
                .orElseGet(() -> settlementRepository.save(Settlement.builder()
                        .groupId(groupId)
                        .fromUser(fromUserId)
                        .toUser(toUserId)
                        .amount(amount)
                        .settled(true)
                        .settledAt(LocalDateTime.now())
                        .build()));
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

        settlementRepository.findByGroupId(groupId)
                .stream()
                .filter(Settlement::isSettled)
                .forEach(settlement -> {
                    netBalances.merge(settlement.getFromUser(), settlement.getAmount(), BigDecimal::add);
                    netBalances.merge(settlement.getToUser(), settlement.getAmount().negate(), BigDecimal::add);
                });

        return netBalances;
    }

    private String buildSettlementKey(String groupId, String fromUserId, String toUserId, BigDecimal amount) {
        return groupId + ":" + fromUserId + ":" + toUserId + ":" + amount.stripTrailingZeros().toPlainString();
    }
}
