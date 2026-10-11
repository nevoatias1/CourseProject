package com.example.store.service;

import com.example.store.model.CustomUser;
import com.example.store.model.Role;
import com.example.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String register(CustomUser user) {
        if (user.getFirstName() == null || user.getLastName() == null || user.getEmail() == null
                || user.getUsername() == null || user.getPassword() == null) {
            return "User not created, first name, last name, email, username and password are required";
        }
        if (getUserByEmail(user.getEmail()) != null || getUserByUsername(user.getUsername()) != null) {
            return "User not created, This email or username already exists in the system.";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return userRepository.register(user);
    }

    public CustomUser getUserByUsername(String username) { return userRepository.findUserByUsername(username); }
    public CustomUser getUserByEmail(String email) { return userRepository.findUserByEmail(email); }
    public List<CustomUser> getAllUsers() { return userRepository.findAllUsers(); }
    public CustomUser updateUser(CustomUser updatedUser) {
        CustomUser currentUser = getUserByUsername(updatedUser.getUsername());
        if (currentUser == null) {
            return null;
        }
        updatedUser.setUsername(currentUser.getUsername());
        updatedUser.setRole(currentUser.getRole());
        if (updatedUser.getPassword() == null || updatedUser.getPassword().isBlank()) {
            updatedUser.setPassword(currentUser.getPassword());
        } else {
            updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        return userRepository.updateUser(updatedUser);
    }

    public String deleteUser(String username) {
        CustomUser registeredUser = userRepository.findUserByUsername(username);
        if (registeredUser == null) {
            return "The user with this username does not exist, so it cannot be deleted";
        }
        return userRepository.deleteUser(registeredUser.getUsername());
    }

    public CustomUser updateUserRole(String username, Role role) {
        if (role == null) {
            return null;
        }
        if (getUserByUsername(username) == null) {
            return null;
        }
        return userRepository.updateUserRole(username, role.name());
    }
}
