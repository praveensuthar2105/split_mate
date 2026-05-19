package com.splitmate.backend.service;

import com.splitmate.backend.model.Expense;
import com.splitmate.backend.model.ExpenseSplit;
import com.splitmate.backend.model.SplitType;
import com.splitmate.backend.repository.ExpenseRepository;
import com.splitmate.backend.repository.ExpenseSplitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final ExpenseCategorizationService expenseCategorizationService;
    private final BudgetService budgetService;

    public Expense createExpense(
            String groupId,
            String description,
            BigDecimal amount,
            String paidBy,
            String category,
            String splitType,
            List<String> participantIds,
            Map<String, BigDecimal> customSplits) {
        validateExpenseInput(groupId, description, amount, paidBy, splitType, participantIds, customSplits);

        Expense expense = Expense.builder()
                .groupId(groupId)
                .description(description)
                .amount(amount)
                .paidBy(paidBy)
                .category(resolveCategory(description, category))
                .splitType(SplitType.valueOf(splitType.toUpperCase()))
                .build();

        expense = expenseRepository.save(expense);

        List<ExpenseSplit> splits = calculateSplits(
                expense.getId(),
                amount,
                splitType,
                participantIds,
                customSplits);

        splits.forEach(expenseSplitRepository::save);

        budgetService.checkBudgetAlert(groupId);

        return expense;
    }

    public Expense getExpense(String expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    public Expense updateExpense(
            String expenseId,
            String description,
            BigDecimal amount,
            String category) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);

        return expenseRepository.save(expense);
    }

    public void deleteExpense(String expenseId) {
        // Delete related splits first
        expenseSplitRepository.findByExpenseId(expenseId)
                .forEach(expenseSplitRepository::delete);

        expenseRepository.deleteById(expenseId);
    }

    public List<Expense> getGroupExpenses(String groupId) {
        return expenseRepository.findByGroupId(groupId);
    }

    public List<ExpenseSplit> getExpenseSplits(String expenseId) {
        return expenseSplitRepository.findByExpenseId(expenseId);
    }

    private String resolveCategory(String description, String category) {
        if (category != null && !category.isBlank()) {
            return category;
        }
        return expenseCategorizationService.categorize(description);
    }

    private List<ExpenseSplit> calculateSplits(
            String expenseId,
            BigDecimal amount,
            String splitType,
            List<String> participantIds,
            Map<String, BigDecimal> customSplits) {
        List<ExpenseSplit> splits = new ArrayList<>();
        SplitType type = SplitType.valueOf(splitType.toUpperCase());

        if (SplitType.EQUAL == type) {
            // Equal split among all participants
            BigDecimal share = amount.divide(
                    BigDecimal.valueOf(participantIds.size()),
                    2,
                    RoundingMode.HALF_UP);

            participantIds.forEach(userId -> splits.add(ExpenseSplit.builder()
                    .expenseId(expenseId)
                    .userId(userId)
                    .shareAmount(share)
                    .build()));

        } else if (SplitType.PERCENTAGE == type) {
            // Custom percentage split
            customSplits.forEach((userId, percentage) -> {
                BigDecimal share = amount.multiply(percentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                splits.add(ExpenseSplit.builder()
                        .expenseId(expenseId)
                        .userId(userId)
                        .shareAmount(share)
                        .build());
            });

        } else if (SplitType.CUSTOM == type) {
            // Custom amount split
            customSplits.forEach((userId, shareAmount) -> splits.add(ExpenseSplit.builder()
                    .expenseId(expenseId)
                    .userId(userId)
                    .shareAmount(shareAmount)
                    .build()));
        }

        return splits;
    }

    private void validateExpenseInput(
            String groupId,
            String description,
            BigDecimal amount,
            String paidBy,
            String splitType,
            List<String> participantIds,
            Map<String, BigDecimal> customSplits) {
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("Group ID is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (paidBy == null || paidBy.isBlank()) {
            throw new IllegalArgumentException("Payer is required");
        }
        if (splitType == null || splitType.isBlank()) {
            throw new IllegalArgumentException("Split type is required");
        }
        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException("At least one participant is required");
        }

        SplitType type = SplitType.valueOf(splitType.toUpperCase());
        if (SplitType.PERCENTAGE == type || SplitType.CUSTOM == type) {
            if (customSplits == null || customSplits.isEmpty()) {
                throw new IllegalArgumentException("Custom splits are required for " + type + " split");
            }
            for (String participantId : participantIds) {
                if (!customSplits.containsKey(participantId)) {
                    throw new IllegalArgumentException("Missing split value for participant: " + participantId);
                }
            }
        }

        if (SplitType.PERCENTAGE == type) {
            BigDecimal totalPercentage = customSplits.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
                throw new IllegalArgumentException("Percentage splits must total 100");
            }
        }

        if (SplitType.CUSTOM == type) {
            BigDecimal totalAmount = customSplits.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalAmount.compareTo(amount) != 0) {
                throw new IllegalArgumentException("Custom splits must total the expense amount");
            }
        }
    }
}
