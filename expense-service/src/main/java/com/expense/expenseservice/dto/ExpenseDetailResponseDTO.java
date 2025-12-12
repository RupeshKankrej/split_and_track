package com.expense.expenseservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExpenseDetailResponseDTO {
    private Long id;
    private BigDecimal amount;
    private String description;
    private String category;
    private Long groupId;
    private Long paidByUserId;
    private List<SplitDefinition> splits;
}