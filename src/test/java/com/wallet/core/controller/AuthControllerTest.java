package com.wallet.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.core.dto.RegisterRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // The tool that simulates HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // Converts Java objects to JSON

    @Test
    void registerUser_withValidData_returnsSuccess() throws Exception {
        // 1. ARRANGE
        RegisterRequestDto request = new RegisterRequestDto();
        request.setName("Test User");
        // Using a random email so the test doesn't fail if run multiple times
        request.setEmail("testuser" + System.currentTimeMillis() + "@example.com");
        request.setPassword("securePassword123");

        // Convert the DTO into a JSON string
        String jsonRequest = objectMapper.writeValueAsString(request);

        // 2. ACT & 3. ASSERT
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                // Using startsWith() because the account number at the end of the string is dynamic
                .andExpect(jsonPath("$.message").value(startsWith("User registered successfully with Account Number")));
    }

    @Test
    void registerUser_withMissingEmail_returnsBadRequest() throws Exception {
        // 1. ARRANGE (Intentionally leaving the email null)
        RegisterRequestDto request = new RegisterRequestDto();
        request.setName("Test User");
        request.setPassword("securePassword123");

        String jsonRequest = objectMapper.writeValueAsString(request);

        // 2. ACT & 3. ASSERT
        // This expects our GlobalExceptionHandler to catch the validation error and return a 400 status!
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }
}