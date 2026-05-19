package com.splitmate.backend.service;

import com.splitmate.backend.model.BotCommand;
import com.splitmate.backend.model.BotCommand.CommandType;
import com.splitmate.backend.model.User;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BotNlpService {

    public BotCommand parse(String text, User user, String activeGroupId) {
        String lower = text.toLowerCase();

        // "Show balance" / "my balance" / "balances"
        if (lower.matches(".*(balance|owe|settle up).*")) {
            return BotCommand.of(CommandType.SHOW_BALANCE);
        }

        // "Settle Priya 340" / "settled with Rohan 500"
        if (lower.matches(".*(settle|settled|paid back).*")) {
            return parseSettleCommand(text);
        }

        // "Use group Goa Squad" / "switch to Flat group"
        if (lower.matches(".*(use group|switch to|set group).*")) {
            return parseSetGroupCommand(text);
        }

        // Default: try to parse as expense
        // "Arjun paid 800 for dinner split 4 ways"
        return parseExpenseCommand(text, user);
    }

    private BotCommand parseSettleCommand(String text) {
        BotCommand cmd = BotCommand.of(CommandType.SETTLE);
        Matcher amountMatcher = Pattern.compile("(\\d+\\.?\\d*)").matcher(text);
        if (amountMatcher.find()) {
            cmd.setAmount(Double.parseDouble(amountMatcher.group(1)));
        }
        return cmd;
    }

    private BotCommand parseSetGroupCommand(String text) {
        return BotCommand.of(CommandType.SET_GROUP);
    }

    private BotCommand parseExpenseCommand(String text, User user) {
        // Extract amount
        Matcher amountMatcher = Pattern
            .compile("(\\d+\\.?\\d*)").matcher(text);
        if (!amountMatcher.find()) {
            return BotCommand.of(CommandType.UNKNOWN);
        }
        double amount = Double.parseDouble(amountMatcher.group(1));

        // Extract description (text after "for")
        String desc = "Expense";
        Matcher forMatcher = Pattern
            .compile("for (.+?)(?:\\s+split|$)", Pattern.CASE_INSENSITIVE)
            .matcher(text);
        if (forMatcher.find()) desc = forMatcher.group(1).trim();

        return BotCommand.builder()
            .type(CommandType.ADD_EXPENSE)
            .amount(amount)
            .description(desc)
            .payer(user.getName() != null ? user.getName() : user.getPhone())
            .build();
    }
}