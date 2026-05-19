package com.splitmate.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to interact with Google Vertex AI / Gemini API.
 * Uses Spring AI abstraction to provide a clean, framework-agnostic interface.
 * Credentials are loaded via GOOGLE_APPLICATION_CREDENTIALS environment variable.
 */
@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    private final ChatClient chatClient;

    @Value("${GOOGLE_CLOUD_PROJECT_ID:}")
    private String projectId;

    @Value("${GEMINI_LOCATION:us-central1}")
    private String location;

    @Value("${GEMINI_MODEL:gemini-2.5-flash}")
    private String modelName;

    /**
     * Constructor: inject ChatModel (Spring AI will auto-configure this from Gemini dependencies).
     * @param chatModel the underlying Gemini chat model
     */
    public GeminiService(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
        logger.info("GeminiService initialized with Spring AI ChatClient");
    }

    /**
     * Call Gemini with a simple text prompt and return the generated text response.
     *
     * @param prompt the input prompt for Gemini
     * @return the generated text response from Gemini
     */
    public String generateContent(String prompt) {
        try {
            logger.debug("Calling Gemini with prompt: {}", prompt);
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            logger.debug("Gemini response: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error calling Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate content from Gemini", e);
        }
    }

    /**
     * Call Gemini with a more detailed configuration (useful for future enhancements).
     * Example: set temperature, max tokens, safety settings, etc.
     *
     * @param prompt the input prompt for Gemini
     * @param temperature controls randomness (0.0 to 2.0, default 1.0)
     * @return the generated text response from Gemini
     */
    public String generateContentWithConfig(String prompt, float temperature) {
        try {
            logger.debug("Calling Gemini with prompt: {} (temperature: {})", prompt, temperature);
            // Spring AI's ChatClient can be extended with additional options in future
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            logger.debug("Gemini response: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error calling Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate content from Gemini", e);
        }
    }

    /**
     * Example use case: categorize an expense description using Gemini.
     * This could be integrated into the ExpenseCategorizationService.
     *
     * @param expenseDescription the description of the expense
     * @return the suggested category
     */
    public String categorizeExpense(String expenseDescription) {
        String prompt = "Categorize the following expense description into one of these categories: " +
                "Food, Transportation, Entertainment, Utilities, Shopping, Other. " +
                "Return only the category name.\n\n" +
                "Expense: " + expenseDescription;
        return generateContent(prompt);
    }

    /**
     * Example use case: generate a settlement summary using Gemini.
     * This could be used to create friendly settlement notifications.
     *
     * @param expenseData structured data about expense
     * @return a friendly summary message
     */
    public String generateSettlementSummary(String expenseData) {
        String prompt = "Create a short, friendly message (1-2 sentences) summarizing this expense split:\n\n" +
                expenseData;
        return generateContent(prompt);
    }
}
