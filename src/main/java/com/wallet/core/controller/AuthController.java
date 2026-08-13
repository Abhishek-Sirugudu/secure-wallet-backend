package com.wallet.core.controller;

import com.wallet.core.dto.LoginRequestDto;
import com.wallet.core.dto.RegisterRequestDto;
import com.wallet.core.entity.User;
import com.wallet.core.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDto request){
        User registeredUser = authService.registerUser(request);

        // Returning as a Map automatically formats it into standard JSON
        String msg = "User registered successfully with Account Number" + registeredUser.getAccount().getAccountNumber();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", msg));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto request){

        String token = authService.Login(request);

        // Returning as a Map automatically formats it into standard JSON
        return ResponseEntity.ok(Map.of("token", token));
    }

}