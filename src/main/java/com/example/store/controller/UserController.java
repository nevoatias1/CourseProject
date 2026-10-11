package com.example.store.controller;

import com.example.store.model.CustomUser;
import com.example.store.service.UserService;
import com.example.store.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public UserController(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody CustomUser user) {
        try {
            String result = userService.register(user);
            if (result.contains("successfully")) {
                return new ResponseEntity<>(result, HttpStatus.CREATED);
            }
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<CustomUser> getUserByUsername(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.extractUsername(token.substring(7));
            return ResponseEntity.ok(userService.getUserByUsername(username));
        } catch (Exception exception) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<CustomUser> updateUser(@RequestHeader("Authorization") String token, @RequestBody CustomUser updatedUser) {
        try {
            String username = jwtUtil.extractUsername(token.substring(7));
            updatedUser.setUsername(username);
            if (updatedUser.getFirstName() == null || updatedUser.getLastName() == null || updatedUser.getEmail() == null) {
                return new ResponseEntity("User not updated, first name, last name and email are required", HttpStatus.BAD_REQUEST);
            }
            CustomUser userFromDB = userService.getUserByUsername(updatedUser.getUsername());
            if (!userFromDB.getEmail().equals(updatedUser.getEmail())) {
                CustomUser sameEmailUser = userService.getUserByEmail(updatedUser.getEmail());
                if (sameEmailUser != null) {
                    return new ResponseEntity("User not updated, This email already exist in the system.", HttpStatus.BAD_REQUEST);
                }
            }
            CustomUser user = userService.updateUser(updatedUser);
            if (user == null) {
                return new ResponseEntity("User not updated. this user does not exist in the system.", HttpStatus.BAD_REQUEST);
            }
            return ResponseEntity.ok(user);
        } catch (Exception exception) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.extractUsername(token.substring(7));
            String result = userService.deleteUser(username);
            if (result.contains("successfully")) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
