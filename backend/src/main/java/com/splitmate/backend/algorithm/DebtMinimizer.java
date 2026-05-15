package com.splitmate.backend.algorithm;

import com.splitmate.backend.dto.SettlementTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

@Component
public class DebtMinimizer {

    private static final BigDecimal CENT = BigDecimal.valueOf(0.01);

    public List<SettlementTransaction> minimize(Map<String, BigDecimal> netBalances) {
        PriorityQueue<Balance> creditors = new PriorityQueue<>(
                Comparator.comparing(Balance::amount).reversed());
        PriorityQueue<Balance> debtors = new PriorityQueue<>(
                Comparator.comparing(Balance::amount).reversed());

        netBalances.forEach((userId, amount) -> {
            BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
            if (normalized.compareTo(CENT) >= 0) {
                creditors.add(new Balance(userId, normalized));
            } else if (normalized.compareTo(CENT.negate()) <= 0) {
                debtors.add(new Balance(userId, normalized.abs()));
            }
        });

        List<SettlementTransaction> transactions = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Balance creditor = creditors.poll();
            Balance debtor = debtors.poll();
            BigDecimal settlementAmount = creditor.amount().min(debtor.amount())
                    .setScale(2, RoundingMode.HALF_UP);

            transactions.add(SettlementTransaction.builder()
                    .fromUserId(debtor.userId())
                    .toUserId(creditor.userId())
                    .amount(settlementAmount)
                    .build());

            BigDecimal creditorLeft = creditor.amount().subtract(settlementAmount);
            BigDecimal debtorLeft = debtor.amount().subtract(settlementAmount);

            if (creditorLeft.compareTo(CENT) >= 0) {
                creditors.add(new Balance(creditor.userId(), creditorLeft));
            }
            if (debtorLeft.compareTo(CENT) >= 0) {
                debtors.add(new Balance(debtor.userId(), debtorLeft));
            }
        }

        return transactions;
    }

    private record Balance(String userId, BigDecimal amount) {
    }
}
