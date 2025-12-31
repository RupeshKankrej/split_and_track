package com.expense.expenseservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expense.expenseservice.client.UserClient;
import com.expense.expenseservice.dto.BalanceDTO;
import com.expense.expenseservice.dto.CategorySpendDTO;
import com.expense.expenseservice.dto.ExpenseDetailResponseDTO;
import com.expense.expenseservice.dto.ExpenseRequestDTO;
import com.expense.expenseservice.dto.ExpenseResponseDTO;
import com.expense.expenseservice.dto.SplitDefinition;
import com.expense.expenseservice.dto.UserDTO;
import com.expense.expenseservice.entity.ExpenseSplit;
import com.expense.expenseservice.entity.Expenses;
import com.expense.expenseservice.entity.Group;
import com.expense.expenseservice.repository.ExpenseRepository;
import com.expense.expenseservice.repository.ExpenseSplitRepository;
import com.expense.expenseservice.repository.GroupRepository;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;
    @Autowired
    private UserClient userClient;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private Map<Long, UserDTO> fetchUserMap(Set<Long> userIds) {
        if (userIds.isEmpty())
            return Collections.emptyMap();

        List<UserDTO> users = userClient.getUsersByIds(new ArrayList(userIds));

        return users.stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));
    }

    @Transactional
    public void addPersonalExpense(Long userId, String desc, BigDecimal amount) {
        UserDTO user = userClient.getUser(userId);

        Expenses expense = new Expenses();
        expense.setDescription(desc);
        expense.setTotalAmount(amount);
        expense.setPaidByUserId(user.getId());
        expense.setCreatedAt(LocalDateTime.now());
        expense = expenseRepository.save(expense);

        ExpenseSplit split = new ExpenseSplit();
        split.setExpense(expense);
        split.setUserId(user.getId());
        split.setAmountOwed(amount);
        expenseSplitRepository.save(split);
    }

    @Transactional
    public void addExpense(ExpenseRequestDTO request) {

        Expenses expense = new Expenses();
        expense.setDescription(request.getDescription());
        expense.setTotalAmount(request.getAmount());
        expense.setPaidByUserId(request.getUserId());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setCategory(request.getCategory() != null ? request.getCategory() : "Other");
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId()).orElse(null);
            expense.setGroup(group);
        }
        expense = expenseRepository.save(expense);

        BigDecimal totalSplitCheck = BigDecimal.ZERO;
        for (SplitDefinition split : request.getSplits()) {
            totalSplitCheck = totalSplitCheck.add(split.getAmount());
        }

        for (SplitDefinition split : request.getSplits()) {

            ExpenseSplit expenseSplit = new ExpenseSplit();
            expenseSplit.setUserId(split.getUserId());
            expenseSplit.setExpense(expense);
            expenseSplit.setAmountOwed(split.getAmount());
            expenseSplitRepository.save(expenseSplit);
        }

        try {
            UserDTO payer = userClient.getUser(request.getUserId());
            System.out.println("Payer found: " + payer.getName());

            for (SplitDefinition split : request.getSplits()) {
                System.out.println("Checking split for UserID: " + split.getUserId());

                // Logic: Don't notify the payer, notify everyone else
                if (!split.getUserId().equals(payer.getId())) {
                    String json = String.format(
                            "{\"targetUserId\": %d, \"amount\": %.2f, \"payerName\": \"%s\"}",
                            split.getUserId(),
                            split.getAmount(),
                            payer.getName());

                    System.out.println("Attempting to send to Kafka: " + json);
                    kafkaTemplate.send("expense-added-topic", json);
                    System.out.println("SUCCESS: Sent to Kafka");
                } else {
                    System.out.println("Skipping notification: User is the payer.");
                }
            }
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR sending notification: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            String description = "You added'" + request.getDescription() + "' for " + request.getAmount();

            String auditJson = String.format(
                    "{\"userId\": %d, \"action\": \"EXPENSE_ADDED\", \"description\": \"%s\"}",
                    request.getUserId(),
                    description);

            kafkaTemplate.send("activity-log-topic", auditJson);
            System.out.println("Sent Audit Log to Kafka");
        } catch (Exception e) {
            System.err.println("Error sending audit log: " + e.getMessage());
        }

    }

    private void createSplit(Expenses expense, UserDTO user, BigDecimal amount) {
        ExpenseSplit split = new ExpenseSplit();
        split.setExpense(expense);
        split.setUserId(user.getId());
        split.setAmountOwed(amount);
        expenseSplitRepository.save(split);
    }

    public List<ExpenseResponseDTO> getUserExpenses(Long userId) {
        List<ExpenseSplit> splits = expenseSplitRepository.findByUserId(userId);

        return mapSplitsToDTO(splits, userId);
    }

    public List<ExpenseResponseDTO> getGroupExpenses(Long groupId, Long userId) {
        List<ExpenseSplit> splits = expenseSplitRepository.findByGroupIdAndUserId(groupId, userId);

        return mapSplitsToDTO(splits, userId);
    }

    public List<CategorySpendDTO> getSpendAnalysisByCategory(Long userId) {
        return expenseSplitRepository.findSpendByCategory(userId);
    }

    public List<ExpenseResponseDTO> mapSplitsToDTO(List<ExpenseSplit> splits, Long currentUserId) {

        return splits.stream().map(split -> {
            Expenses e = split.getExpense();
            UserDTO payer = userClient.getUser(e.getPaidByUserId());
            ExpenseResponseDTO dto = new ExpenseResponseDTO();
            dto.setExpenseId(e.getId());
            dto.setDescription(e.getDescription());
            dto.setTotalAmount(e.getTotalAmount());
            dto.setMyShare(split.getAmountOwed());
            dto.setDate(e.getCreatedAt());
            dto.setPaidByUserId(payer.getId());

            if (payer.getId().equals(currentUserId)) {
                dto.setPaidByName("You");
            } else {
                dto.setPaidByName(payer.getName());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public List<ExpenseResponseDTO> getGroupExpense(Long groupId, Long currentUserId) {
        List<Expenses> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        if (expenses.isEmpty())
            return new ArrayList<>();

        List<ExpenseSplit> allGroupSplits = expenseSplitRepository.findByGroupId(groupId);

        Map<Long, List<ExpenseSplit>> splitsByExpenseId = allGroupSplits.stream()
                .collect(Collectors.groupingBy(s -> s.getExpense().getId()));

        Set<Long> userIdsToFetch = new HashSet<>();
        userIdsToFetch.add(currentUserId);
        for (Expenses e : expenses) {
            userIdsToFetch.add(e.getPaidByUserId());
        }
        Map<Long, UserDTO> userMap = fetchUserMap(userIdsToFetch);

        return expenses.stream().map(e -> {
            ExpenseResponseDTO dto = new ExpenseResponseDTO();
            dto.setExpenseId(e.getId());
            dto.setDescription(e.getDescription());
            dto.setTotalAmount(e.getTotalAmount());
            dto.setDate(e.getCreatedAt());
            dto.setPaidByUserId(e.getPaidByUserId());

            if (e.getPaidByUserId().equals(currentUserId)) {
                dto.setPaidByName("You");
            } else {
                UserDTO u = userMap.get(e.getPaidByUserId());
                dto.setPaidByName(u != null ? u.getName() : "Unknown");
            }

            List<ExpenseSplit> currentSplits = splitsByExpenseId.getOrDefault(e.getId(), Collections.emptyList());

            BigDecimal myShare = currentSplits.stream()
                    .filter(s -> s.getUserId().equals(currentUserId))
                    .map(ExpenseSplit::getAmountOwed)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dto.setMyShare(myShare);
            boolean isPayer = e.getPaidByUserId().equals(currentUserId);
            boolean hasShare = myShare.compareTo(BigDecimal.ZERO) > 0;
            dto.setInvolved(isPayer || hasShare);

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        expenseSplitRepository.deleteByExpenseId(expenseId);
        expenseRepository.deleteById(expenseId);
    }

    @Transactional
    public void updateExpense(Long expenseId, ExpenseRequestDTO request) {
        Expenses expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setDescription(request.getDescription());
        expense.setTotalAmount(request.getAmount());
        expense.setCategory(request.getCategory());

        expenseRepository.save(expense);

        List<ExpenseSplit> oldSplits = expenseSplitRepository.findAll().stream()
                .filter(s -> s.getExpense().getId().equals(expenseId))
                .collect(Collectors.toList());
        expenseSplitRepository.deleteAll(oldSplits);

        for (SplitDefinition splitDef : request.getSplits()) {
            UserDTO user = userClient.getUser(splitDef.getUserId());
            ExpenseSplit split = new ExpenseSplit();
            split.setExpense(expense);
            split.setUserId(user.getId());
            split.setAmountOwed(splitDef.getAmount());
            expenseSplitRepository.save(split);
        }
    }

    public ExpenseDetailResponseDTO getExpenseDetails(Long expenseId) {
        Expenses expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Fetch Splits
        List<ExpenseSplit> splits = expenseSplitRepository.findAll().stream()
                .filter(s -> s.getExpense().getId().equals(expenseId))
                .collect(Collectors.toList());

        // 1. Collect IDs
        Set<Long> userIds = splits.stream().map(ExpenseSplit::getUserId).collect(Collectors.toSet());
        userIds.add(expense.getPaidByUserId());

        // 2. Fetch User Details
        Map<Long, UserDTO> userMap = fetchUserMap(userIds);

        ExpenseDetailResponseDTO dto = new ExpenseDetailResponseDTO();
        dto.setId(expense.getId());
        dto.setAmount(expense.getTotalAmount());
        dto.setDescription(expense.getDescription());
        dto.setCategory(expense.getCategory());
        dto.setPaidByUserId(expense.getPaidByUserId());
        if (expense.getGroup() != null)
            dto.setGroupId(expense.getGroup().getId());

        List<SplitDefinition> splitDefs = splits.stream().map(s -> {
            SplitDefinition sd = new SplitDefinition();
            sd.setUserId(s.getUserId());

            // Resolve Name
            UserDTO u = userMap.get(s.getUserId());
            sd.setUserName(u != null ? u.getName() : "Unknown");

            sd.setAmount(s.getAmountOwed());
            return sd;
        }).collect(Collectors.toList());

        dto.setSplits(splitDefs);
        return dto;
    }

    public List<BalanceDTO> getGroupBalances(Long groupId, Long currentUserId) {
        List<ExpenseSplit> allSplits = groupRepository.findGroupSplits(groupId);

        // 1. Optimization: Collect all Unique IDs involved
        Set<Long> uniqueUserIds = new HashSet<>();
        uniqueUserIds.add(currentUserId);
        for (ExpenseSplit s : allSplits) {
            uniqueUserIds.add(s.getUserId()); // Debtor
            uniqueUserIds.add(s.getExpense().getPaidByUserId()); // Payer
        }

        // 2. Fetch User Names in ONE CALL
        Map<Long, UserDTO> userMap = fetchUserMap(uniqueUserIds);

        Map<Long, BigDecimal> balances = new HashMap<>();
        Map<Long, String> userNames = new HashMap<>();

        for (ExpenseSplit split : allSplits) {
            Long payerId = split.getExpense().getPaidByUserId();
            Long debtorId = split.getUserId();
            BigDecimal amountOwed = split.getAmountOwed();

            // Case 1: I paid, someone else owes me
            if (payerId.equals(currentUserId) && !debtorId.equals(currentUserId)) {
                balances.merge(debtorId, amountOwed, BigDecimal::add);

                UserDTO u = userMap.get(debtorId);
                String name = (u != null) ? u.getName() : "User " + debtorId;
                userNames.put(debtorId, name);
            }

            // Case 2: Someone else paid, I owe them
            if (!payerId.equals(currentUserId) && debtorId.equals(currentUserId)) {
                balances.merge(payerId, amountOwed.negate(), BigDecimal::add);

                UserDTO u = userMap.get(payerId);
                String name = (u != null) ? u.getName() : "User " + payerId;
                userNames.put(payerId, name);
            }
        }

        List<BalanceDTO> result = new ArrayList<>();
        balances.forEach((userId, amount) -> {
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
                String name = userNames.getOrDefault(userId, "Unknown");
                result.add(new BalanceDTO(userId, name, amount));
            }
        });

        return result;
    }

}
