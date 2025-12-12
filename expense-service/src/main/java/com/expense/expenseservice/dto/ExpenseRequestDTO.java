package com.expense.expenseservice.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ExpenseRequestDTO {
    private Long userId;
    private BigDecimal amount;
    private String description;
    private String category;
    private Long groupId;

    private List<SplitDefinition> splits;
}
