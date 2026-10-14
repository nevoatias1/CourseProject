package com.example.store.controller;

import com.example.store.model.AuthenticationRequest;
import com.example.store.model.AuthenticationResponse;
import com.example.store.model.ApiErrorResponse;
import com.example.store.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            AuthenticationResponse authResponse = authenticationService.createAuthenticationToken(authenticationRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiErrorResponse("Incorrect username or password"));
        }
    }
}
