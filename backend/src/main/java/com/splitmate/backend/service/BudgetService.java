package com.splitmate.backend.service;

import com.splitmate.backend.model.GroupBudget;
import com.splitmate.backend.repository.ExpenseRepository;
import com.splitmate.backend.repository.GroupBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final GroupBudgetRepository budgetRepo;
    private final ExpenseRepository expenseRepo;
    private final FcmService fcmService;

    public void checkBudgetAlert(String groupId) {
        GroupBudget budget = budgetRepo.findByGroupId(groupId).orElse(null);
        if (budget == null) return;

        BigDecimal totalSpend = expenseRepo.sumAmountByGroupId(groupId);
        if (totalSpend == null) totalSpend = BigDecimal.ZERO;

        double pct = totalSpend
            .divide(budget.getTotalAmount(), 4, RoundingMode.HALF_UP)
            .doubleValue() * 100;

        if (pct >= 100 && !budget.isAlert100Sent()) {
            fcmService.sendToGroup(groupId,
                "Budget Exceeded \uD83DD\uDD34",
                "Your group has spent ₹" + totalSpend +
                " — over the ₹" + budget.getTotalAmount() + " budget");
            budget.setAlert100Sent(true);
            budgetRepo.save(budget);
        } else if (pct >= 80 && !budget.isAlert80Sent()) {
            fcmService.sendToGroup(groupId,
                "Budget Alert \uD83DF\uDFE1",
                "Your group has used " + (int)pct + "% of the budget");
            budget.setAlert80Sent(true);
            budgetRepo.save(budget);
        }
    }
}