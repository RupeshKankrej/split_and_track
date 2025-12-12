package com.expense.identityservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expense.identityservice.dto.UserDTO;
import com.expense.identityservice.entity.User;
import com.expense.identityservice.entity.UserStatus;
import com.expense.identityservice.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    public UserRepository userRepo;
    @Autowired
    public EmailService emailService;

    public UserDTO getUserById(Long userId) {
        return userRepo.findById(userId).map(this::mapToDTO).orElse(null);
    }

    public List<UserDTO> getUsersByIds(List<Long> userIds) {
        List<User> users = userRepo.findAllById(userIds);

        return users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO inviteUser(String email, String name) {
        User existingUser = userRepo.findByEmail(email);

        if (existingUser != null) {
            return mapToDTO(existingUser); // Return existing user
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name != null ? name : "Invited User");
        newUser.setStatus(UserStatus.INVITED);

        User savedUser = userRepo.save(newUser);

        // Disabled for now
        // emailService.sendInviteEmail(email, name);

        return mapToDTO(savedUser);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepo.findAll();
        return users.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Helper to map Entity -> DTO
    private UserDTO mapToDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }

}
