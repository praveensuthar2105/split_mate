package com.splitmate.backend.controller;

import com.splitmate.backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API endpoint for Gemini AI features.
 * Examples: categorizing expenses, generating summaries, etc.
 */
@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    /**
     * Test endpoint: generate content from a simple prompt.
     * Example: POST /api/gemini/generate with {"prompt": "What is 2 + 2?"}
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateContent(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "prompt is required")
            );
        }

        try {
            String response = geminiService.generateContent(prompt);
            return ResponseEntity.ok(Map.of(
                    "prompt", prompt,
                    "response", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Failed to generate content: " + e.getMessage())
            );
        }
    }

    /**
     * Endpoint: categorize an expense using Gemini.
     * Example: POST /api/gemini/categorize-expense with {"description": "Dinner at Pizza Hut"}
     */
    @PostMapping("/categorize-expense")
    public ResponseEntity<?> categorizeExpense(@RequestBody Map<String, String> body) {
        String description = body.get("description");
        if (description == null || description.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "description is required")
            );
        }

        try {
            String category = geminiService.categorizeExpense(description);
            return ResponseEntity.ok(Map.of(
                    "description", description,
                    "category", category
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Failed to categorize: " + e.getMessage())
            );
        }
    }

    /**
     * Endpoint: generate a settlement summary using Gemini.
     * Example: POST /api/gemini/settlement-summary with {"expenseData": "Alice paid 500, split between Bob and Charlie"}
     */
    @PostMapping("/settlement-summary")
    public ResponseEntity<?> settlementSummary(@RequestBody Map<String, String> body) {
        String expenseData = body.get("expenseData");
        if (expenseData == null || expenseData.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "expenseData is required")
            );
        }

        try {
            String summary = geminiService.generateSettlementSummary(expenseData);
            return ResponseEntity.ok(Map.of(
                    "expenseData", expenseData,
                    "summary", summary
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Failed to generate summary: " + e.getMessage())
            );
        }
    }
}
