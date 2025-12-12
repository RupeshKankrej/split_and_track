package com.expense.expenseservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.expense.expenseservice.entity.Expenses;

@Repository
public interface ExpenseRepository extends JpaRepository<Expenses, Long> {

    List<Expenses> findByGroupIdOrderByCreatedAtDesc(Long group);
}
