package com.splitmate.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseCategorizationServiceTest {

    private final ExpenseCategorizationService service = new ExpenseCategorizationService(null);

    @Test
    void categorizesCommonExpenseDescriptions() {
        assertThat(service.categorize("pizza with friends")).isEqualTo("Food");
        assertThat(service.categorize("uber to station")).isEqualTo("Transport");
        assertThat(service.categorize("monthly flat rent")).isEqualTo("Rent");
        assertThat(service.categorize("milk and rice from supermarket")).isEqualTo("Groceries");
        assertThat(service.categorize("movie tickets")).isEqualTo("Entertainment");
        assertThat(service.categorize("electricity bill")).isEqualTo("Utilities");
    }

    @Test
    void returnsOtherForUnknownOrBlankDescriptions() {
        assertThat(service.categorize("random transfer")).isEqualTo("Other");
        assertThat(service.categorize(" ")).isEqualTo("Other");
        assertThat(service.categorize(null)).isEqualTo("Other");
    }
}
