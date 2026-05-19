package com.splitmate.backend.controller;

import com.splitmate.backend.model.BotCommand;
import com.splitmate.backend.model.User;
import com.splitmate.backend.repository.UserRepository;
import com.splitmate.backend.service.BotNlpService;
import com.splitmate.backend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;

@RestController
@RequestMapping("/bot/whatsapp")
@RequiredArgsConstructor
public class WhatsAppBotController {

    private final BotNlpService nlpService;
    private final ExpenseService expenseService;
    private final UserRepository userRepo;
    private final RedisTemplate<String, String> redis;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleMessage(
        @RequestParam("From") String from,      // whatsapp:+919876543210
        @RequestParam("Body") String body
    ) {
        String phone = from.replace("whatsapp:+91", "").replace("whatsapp:", "");
        User user = userRepo.findByPhone(phone).orElse(null);

        if (user == null) {
            return twilioReply("Your number is not linked to SplitMate. " +
                "Open the app → Settings → Link WhatsApp.");
        }

        // Get user's active group from Redis session
        String activeGroupId = redis.opsForValue().get("bot:group:" + user.getId());

        BotCommand command = nlpService.parse(body.trim(), user, activeGroupId);

        String reply = switch (command.getType()) {
            case ADD_EXPENSE -> handleAddExpense(command, user, activeGroupId);
            case SHOW_BALANCE -> handleShowBalance(user, activeGroupId);
            case SETTLE       -> handleSettle(command, user, activeGroupId);
            case SET_GROUP    -> handleSetGroup(command, user);
            default           -> "I didn't understand that. Try:\n" +
                                 "• \"Arjun paid 800 for dinner split 4\"\n" +
                                 "• \"Show balances\"\n" +
                                 "• \"Settle Priya 340\"";
        };

        return twilioReply(reply);
    }

    private ResponseEntity<String> twilioReply(String message) {
        String twiml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Response><Message>%s</Message></Response>
            """.formatted(message);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(twiml);
    }

    private String handleAddExpense(BotCommand cmd, User user, String groupId) {
        if (groupId == null) return "No active group. Set one first: \"Use group [name]\"";
        try {
            expenseService.createExpense(
                groupId,
                cmd.getDescription(),
                BigDecimal.valueOf(cmd.getAmount()),
                user.getId(),
                "other",
                "EQUAL",
                Collections.singletonList(user.getId()), // Need group members ideally
                null
            );
            return "✅ Added: " + cmd.getDescription() +
                   " · ₹" + cmd.getAmount() +
                   " paid by " + cmd.getPayer();
        } catch (Exception e) {
            return "Failed to add expense: " + e.getMessage();
        }
    }

    private String handleShowBalance(User user, String activeGroupId) {
        return "Balance feature coming soon!";
    }

    private String handleSettle(BotCommand cmd, User user, String activeGroupId) {
        return "Settle feature coming soon!";
    }

    private String handleSetGroup(BotCommand cmd, User user) {
        return "Set group feature coming soon!";
    }
}