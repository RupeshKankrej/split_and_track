package com.expense.expenseservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expense.expenseservice.dto.CategorySpendDTO;
import com.expense.expenseservice.dto.ExpenseDetailResponseDTO;
import com.expense.expenseservice.dto.ExpenseRequestDTO;
import com.expense.expenseservice.dto.ExpenseResponseDTO;
import com.expense.expenseservice.service.ExpenseService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<String> addExpense(@RequestBody ExpenseRequestDTO request) {
        expenseService.addExpense(request);
        return ResponseEntity.ok("Expense saved and split successfully!");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExpenseResponseDTO>> getMyExpenses(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.getUserExpenses(userId));
    }

    @GetMapping("/group/{groupId}/{userId}")
    public ResponseEntity<List<ExpenseResponseDTO>> getGroupExpenses(@PathVariable Long groupId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.getGroupExpense(groupId, userId));

    }

    @GetMapping("/analysis/{userId}")
    public ResponseEntity<List<CategorySpendDTO>> getSpendAnalysis(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.getSpendAnalysisByCategory(userId));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<String> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.ok("Expense deleted");
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<String> updateExpense(@PathVariable long expenseId, @RequestBody ExpenseRequestDTO request) {
        expenseService.updateExpense(expenseId, request);
        return ResponseEntity.ok("Expense Updated");
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDetailResponseDTO> getExpenseDetails(@PathVariable Long expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseDetails(expenseId));
    }

    // Endpoint: GET /api/expenses/test
    // Use this to check if your phone can see your laptop
    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("Connection Successful!");
    }
}
