package com.kstefancic.lotterymaster.controller;

import com.kstefancic.lotterymaster.repository.UserRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public HttpEntity<?> getAuthenticatedUser(){
        return ResponseEntity.ok(userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()));
    }
}
