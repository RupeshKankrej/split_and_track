package com.expense.expenseservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.expense.expenseservice.entity.ExpenseSplit;
import com.expense.expenseservice.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g JOIN g.memberIds m WHERE m = :userId")
    List<Group> findGroupsByMemberId(@Param("userId") Long userId);

    @Query("SELECT s FROM ExpenseSplit s " +
            "JOIN FETCH s.expense e " +
            "WHERE e.group.id = :groupId")
    List<ExpenseSplit> findGroupSplits(@Param("groupId") Long groupId);
}
