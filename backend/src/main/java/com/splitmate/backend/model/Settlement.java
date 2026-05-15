package com.splitmate.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "settlements")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String groupId;
    private String fromUser;
    private String toUser;
    private BigDecimal amount;
    private boolean settled;
    private LocalDateTime settledAt;
}
