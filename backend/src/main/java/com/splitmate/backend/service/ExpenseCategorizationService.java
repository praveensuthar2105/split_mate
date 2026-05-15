package com.splitmate.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ExpenseCategorizationService {

    private static final List<String> CATEGORIES = List.of(
            "Food",
            "Transport",
            "Rent",
            "Groceries",
            "Entertainment",
            "Utilities",
            "Other");

    private static final Map<String, List<String>> KEYWORDS = Map.of(
            "Food", List.of("food", "restaurant", "dinner", "lunch", "breakfast", "pizza", "burger", "coffee",
                    "cafe", "swiggy", "zomato", "meal", "snacks"),
            "Transport", List.of("uber", "ola", "taxi", "cab", "bus", "train", "metro", "flight", "petrol",
                    "diesel", "fuel", "parking", "toll", "auto", "rickshaw"),
            "Rent", List.of("rent", "flat", "apartment", "pg", "hostel", "room"),
            "Groceries", List.of("grocery", "groceries", "milk", "vegetable", "vegetables", "rice", "atta",
                    "dal", "supermarket", "mart"),
            "Entertainment", List.of("movie", "cinema", "netflix", "prime", "hotstar", "game", "concert",
                    "party", "tickets"),
            "Utilities", List.of("electricity", "water", "wifi", "internet", "broadband", "gas", "recharge",
                    "mobile", "bill"));

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    public ExpenseCategorizationService(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
    }

    public String categorize(String description) {
        if (description == null || description.isBlank()) {
            return "Other";
        }

        String aiCategory = categorizeWithAi(description);
        if (aiCategory != null) {
            return aiCategory;
        }

        return categorizeWithKeywords(description);
    }

    public List<String> getSupportedCategories() {
        return CATEGORIES;
    }

    private String categorizeWithAi(String description) {
        if (chatClientBuilderProvider == null) {
            return null;
        }

        try {
            ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
            if (builder == null) {
                return null;
            }

            String response = builder.build()
                    .prompt()
                    .system("""
                            You categorize SplitMate expenses.
                            Return exactly one category from this list:
                            Food, Transport, Rent, Groceries, Entertainment, Utilities, Other.
                            Do not return explanations or extra text.
                            """)
                    .user(description)
                    .call()
                    .content();

            return normalizeCategory(response);
        } catch (Exception e) {
            return null;
        }
    }

    private String categorizeWithKeywords(String description) {
        String normalizedDescription = description.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalizedDescription.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return "Other";
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }

        String normalized = category.trim();
        return CATEGORIES.stream()
                .filter(supportedCategory -> supportedCategory.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
    }
}
