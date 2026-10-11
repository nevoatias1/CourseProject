package com.example.store.service;

import com.example.store.model.AuthenticationRequest;
import com.example.store.model.AuthenticationResponse;
import com.example.store.security.CustomUserDetailsService;
import com.example.store.utils.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthenticationResponse createAuthenticationToken(AuthenticationRequest authenticationRequest) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        if (!passwordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Incorrect password");
        }
        return new AuthenticationResponse(jwtUtil.generateToken(userDetails));
    }
}
