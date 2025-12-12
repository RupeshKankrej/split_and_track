package com.expense.expenseservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceDTO {

    private Long userId;
    private String userName;
    private BigDecimal amount;
}
