package com.expense.expenseservice.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.expense.expenseservice.dto.UserDTO;

@FeignClient(name = "IDENTITY-SERVICE")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserDTO getUser(@PathVariable("id") Long id);

    @PostMapping("/api/users/batch")
    List<UserDTO> getUsersByIds(@RequestBody List<Long> userIds);

    @PostMapping("/api/users/invite")
    UserDTO inviteUser(@RequestBody Map<String, String> payload);
}
