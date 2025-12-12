package com.expense.expenseservice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.expense.expenseservice.client.UserClient;
import com.expense.expenseservice.dto.BalanceDTO;
import com.expense.expenseservice.dto.UserDTO;
import com.expense.expenseservice.entity.Group;
import com.expense.expenseservice.repository.GroupRepository;
import com.expense.expenseservice.service.ExpenseService;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserClient userClient;

    @PostMapping("/create")
    public ResponseEntity<Group> createGroup(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        Long adminId = ((Number) payload.get("userId")).longValue();

        Group group = new Group();
        group.setName(name);
        group.setCreatedByUserId(adminId);

        List<Long> memberIds = new ArrayList<>();
        memberIds.add(adminId);

        if (payload.containsKey("memberIds")) {
            List<?> rawIds = (List<?>) payload.get("memberIds");
            for (Object rawId : rawIds) {
                Long memberId = ((Number) rawId).longValue();

                if (!memberIds.contains(memberId)) {
                    memberIds.add(memberId);
                }
            }
        }

        group.setMemberIds(memberIds);
        return ResponseEntity.ok(groupRepo.save(group));
    }

    @PostMapping("/{groupId}/addMembers")
    public ResponseEntity<Group> addMembers(@PathVariable Long groupId, @RequestBody Map<String, String> payload) {
        Group group = groupRepo.findById(groupId).orElseThrow();

        String email = payload.get("email");
        String name = payload.get("name");

        UserDTO user = userClient.inviteUser(Map.of("email", email, "name", name != null ? name : "Friend"));

        if (!group.getMemberIds().contains(user.getId())) {
            group.getMemberIds().add(user.getId());
        }

        return ResponseEntity.ok(groupRepo.save(group));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Group>> getMyGroups(@PathVariable Long userId) {

        return ResponseEntity.ok(groupRepo.findGroupsByMemberId(userId));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroup(@PathVariable Long groupId) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        return ResponseEntity.ok(group);
    }

    @GetMapping("/{groupId}/balances/{userId}")
    public ResponseEntity<List<BalanceDTO>> getGroupBalances(@PathVariable("groupId") Long groupId,
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(expenseService.getGroupBalances(groupId, userId));
    }
}