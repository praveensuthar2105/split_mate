package com.splitmate.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expenses")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String groupId;
    private String description;
    private BigDecimal amount;
    private String paidBy;
    private String category;

    @Enumerated(EnumType.STRING)
    private SplitType splitType;  // EQUAL, PERCENTAGE, CUSTOM

    private LocalDate date;
    private boolean isRecurring;
    private String recurrenceInterval; // WEEKLY, MONTHLY

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
