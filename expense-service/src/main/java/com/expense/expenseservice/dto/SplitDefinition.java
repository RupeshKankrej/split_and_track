package com.expense.expenseservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SplitDefinition {
    private Long userId;
    private String userName;
    private BigDecimal amount;
}
