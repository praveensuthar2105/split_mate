package com.splitmate.backend.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementTransaction {
    private String id;
    private String fromUserId;
    private String toUserId;
    private BigDecimal amount;
    private boolean settled;
}
