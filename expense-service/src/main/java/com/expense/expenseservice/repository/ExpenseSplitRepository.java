package com.expense.expenseservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.expense.expenseservice.dto.CategorySpendDTO;
import com.expense.expenseservice.entity.ExpenseSplit;

import jakarta.transaction.Transactional;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {

    @Query("SELECT s FROM ExpenseSplit s JOIN FETCH s.expense e WHERE s.userId = :userId ORDER BY e.createdAt DESC")
    List<ExpenseSplit> findByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM ExpenseSplit s JOIN s.expense e WHERE s.userId = :userId and e.group.id = :groupId ORDER BY e.createdAt DESC")
    List<ExpenseSplit> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Query("SELECT s FROM ExpenseSplit s JOIN s.expense e WHERE e.group.id = :groupId ORDER BY e.createdAt DESC")
    List<ExpenseSplit> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT new com.expense.expenseservice.dto.CategorySpendDTO(e.category, SUM(s.amountOwed)) " +
            "FROM ExpenseSplit s " +
            "JOIN s.expense e " +
            "WHERE s.userId = :userId " +
            "GROUP BY e.category")
    List<CategorySpendDTO> findSpendByCategory(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExpenseSplit s WHERE s.expense.id = :expenseId")
    public void deleteByExpenseId(@Param("expenseId") Long expenseId);
}
