package com.splitmate.backend.controller;

import com.splitmate.backend.model.Expense;
import com.splitmate.backend.model.ExpenseSplit;
import com.splitmate.backend.security.JwtUtil;
import com.splitmate.backend.service.ExpenseCategorizationService;
import com.splitmate.backend.service.ExpenseService;
import com.splitmate.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseCategorizationService expenseCategorizationService;
    private final GroupService groupService;
    private final JwtUtil jwtUtil;

    @PostMapping("/expenses/categorize")
    public ResponseEntity<?> categorizeExpense(@RequestBody Map<String, String> body) {
        String description = body.get("description");
        if (description == null || description.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Description is required"));
        }

        return ResponseEntity.ok(Map.of(
                "description", description,
                "category", expenseCategorizationService.categorize(description),
                "supportedCategories", expenseCategorizationService.getSupportedCategories()));
    }

    @PostMapping("/expenses")
    public ResponseEntity<?> createExpense(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        return createExpenseForGroup(authHeader, (String) body.get("groupId"), body);
    }

    @PostMapping("/groups/{groupId}/expenses")
    public ResponseEntity<?> createGroupExpense(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        return createExpenseForGroup(authHeader, groupId, body);
    }

    @GetMapping("/groups/{groupId}/expenses")
    public ResponseEntity<?> getGroupExpenses(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserId(authHeader);
            verifyGroupAccess(groupId, userId);

            List<Expense> expenses = expenseService.getGroupExpenses(groupId);
            return ResponseEntity.ok(expenses);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @GetMapping("/expenses/group/{groupId}")
    public ResponseEntity<?> getGroupExpensesLegacy(
            @PathVariable String groupId,
            @RequestHeader("Authorization") String authHeader) {
        return getGroupExpenses(groupId, authHeader);
    }

    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<?> getExpense(
            @PathVariable String expenseId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserId(authHeader);
            Expense expense = expenseService.getExpense(expenseId);
            verifyGroupAccess(expense.getGroupId(), userId);

            List<ExpenseSplit> splits = expenseService.getExpenseSplits(expenseId);

            return ResponseEntity.ok(Map.of(
                    "expense", expense,
                    "splits", splits));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @PutMapping("/expenses/{expenseId}")
    public ResponseEntity<?> updateExpense(
            @PathVariable String expenseId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            String userId = extractUserId(authHeader);
            Expense existing = expenseService.getExpense(expenseId);
            verifyGroupAccess(existing.getGroupId(), userId);

            String description = body.containsKey("description")
                    ? (String) body.get("description")
                    : existing.getDescription();
            BigDecimal amount = body.containsKey("amount")
                    ? toBigDecimal(body.get("amount"))
                    : existing.getAmount();
            String category = body.containsKey("category")
                    ? (String) body.get("category")
                    : existing.getCategory();

            Expense expense = expenseService.updateExpense(
                    expenseId,
                    description,
                    amount,
                    category);

            return ResponseEntity.ok(expense);

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<?> deleteExpense(
            @PathVariable String expenseId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserId(authHeader);
            Expense expense = expenseService.getExpense(expenseId);
            verifyGroupAccess(expense.getGroupId(), userId);

            expenseService.deleteExpense(expenseId);

            return ResponseEntity.ok(Map.of("message", "Expense deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }
    }

    private ResponseEntity<?> createExpenseForGroup(
            String authHeader,
            String groupId,
            Map<String, Object> body) {
        try {
            String userId = extractUserId(authHeader);
            if (groupId == null || groupId.isBlank()) {
                throw new IllegalArgumentException("Group ID is required");
            }
            verifyGroupAccess(groupId, userId);

            String description = (String) body.get("description");
            BigDecimal amount = toBigDecimal(body.get("amount"));
            String paidBy = (String) body.get("paidBy");
            String category = (String) body.get("category");
            String splitType = (String) body.get("splitType");
            List<String> participantIds = toStringList(body.get("participantIds"));
            Map<String, BigDecimal> customSplits = toBigDecimalMap(body.get("customSplits"));

            Expense expense = expenseService.createExpense(
                    groupId,
                    description,
                    amount,
                    paidBy,
                    category,
                    splitType,
                    participantIds,
                    customSplits);

            return ResponseEntity.status(HttpStatus.CREATED).body(expense);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
    }

    private String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        return jwtUtil.extractUserId(token);
    }

    private void verifyGroupAccess(String groupId, String userId) {
        if (groupId == null || !groupService.isGroupMember(groupId, userId)) {
            throw new RuntimeException("Group not accessible");
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        return new BigDecimal(value.toString());
    }

    private List<String> toStringList(Object value) {
        if (!(value instanceof List<?> rawList)) {
            return null;
        }
        return rawList.stream()
                .map(Object::toString)
                .toList();
    }

    private Map<String, BigDecimal> toBigDecimalMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return null;
        }

        Map<String, BigDecimal> result = new HashMap<>();
        rawMap.forEach((key, mapValue) -> result.put(key.toString(), toBigDecimal(mapValue)));
        return result;
    }
}
