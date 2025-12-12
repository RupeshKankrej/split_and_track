package com.expense.expenseservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategorySpendDTO {

    private String category;
    private BigDecimal totalSpend;
}
