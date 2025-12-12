package com.expense.expenseservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "t_expenses")
@Data
public class Expenses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String category;

    @JoinColumn(name = "paid_by_user_id")
    private Long paidByUserId;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
}
