package com.expense.expenseservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ExpenseResponseDTO {

    private Long expenseId;
    private String description;
    private BigDecimal totalAmount;
    private BigDecimal myShare;
    private String paidByName;
    private Long paidByUserId;
    private LocalDateTime date;

    private boolean involved;
}