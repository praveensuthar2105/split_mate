package com.splitmate.backend.algorithm;

import com.splitmate.backend.dto.SettlementTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DebtMinimizerTest {

    private final DebtMinimizer debtMinimizer = new DebtMinimizer();

    @Test
    void createsTwoTransactionsForOnePayerAndTwoDebtors() {
        Map<String, BigDecimal> balances = Map.of(
                "A", BigDecimal.valueOf(200),
                "B", BigDecimal.valueOf(-100),
                "C", BigDecimal.valueOf(-100));

        List<SettlementTransaction> result = debtMinimizer.minimize(balances);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(SettlementTransaction::getToUserId)
                .containsOnly("A");
        assertThat(result)
                .extracting(SettlementTransaction::getAmount)
                .containsExactlyInAnyOrder(BigDecimal.valueOf(100).setScale(2), BigDecimal.valueOf(100).setScale(2));
    }

    @Test
    void returnsNoTransactionsWhenAlreadyBalanced() {
        Map<String, BigDecimal> balances = Map.of(
                "A", BigDecimal.ZERO,
                "B", BigDecimal.ZERO,
                "C", BigDecimal.ZERO);

        List<SettlementTransaction> result = debtMinimizer.minimize(balances);

        assertThat(result).isEmpty();
    }

    @Test
    void minimizesMultipleDebtsToAtMostMembersMinusOneTransactions() {
        Map<String, BigDecimal> balances = Map.of(
                "A", BigDecimal.valueOf(500),
                "B", BigDecimal.valueOf(200),
                "C", BigDecimal.valueOf(-300),
                "D", BigDecimal.valueOf(-400));

        List<SettlementTransaction> result = debtMinimizer.minimize(balances);

        assertThat(result).hasSizeLessThanOrEqualTo(3);
        assertThat(result)
                .extracting(SettlementTransaction::getAmount)
                .containsExactlyInAnyOrder(
                        BigDecimal.valueOf(400).setScale(2),
                        BigDecimal.valueOf(100).setScale(2),
                        BigDecimal.valueOf(200).setScale(2));
        assertThat(total(result)).isEqualByComparingTo(BigDecimal.valueOf(700));
    }

    private BigDecimal total(List<SettlementTransaction> transactions) {
        return transactions.stream()
                .map(SettlementTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
