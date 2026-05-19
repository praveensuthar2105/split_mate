package com.splitmate.backend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BotCommand {

    public enum CommandType {
        ADD_EXPENSE,
        SHOW_BALANCE,
        SETTLE,
        SET_GROUP,
        UNKNOWN
    }

    private CommandType type;
    private double amount;
    private String description;
    private String payer;
    private String groupId;

    // For settlements
    private String settleWithUser;

    public static BotCommand of(CommandType type) {
        return BotCommand.builder().type(type).build();
    }

}