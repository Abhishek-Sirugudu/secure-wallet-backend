package com.wallet.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.core.dto.DepositRequestDto;
import com.wallet.core.entity.Account;
import com.wallet.core.entity.User;
import com.wallet.core.repository.UserRepository;
import com.wallet.core.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // @MockBean completely replaces the real Spring beans with Mockito fakes for this test.
    // This allows us to test the Controller's web routing without hitting the real database.
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransactionService transactionService;

    @Test
    @WithMockUser(username = "alicejohnson@example.com", roles = "USER")
    void depositFunds_withValidRequest_returnsSuccess() throws Exception {
        // 1. ARRANGE
        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(new BigDecimal("5000.00"));

        String jsonRequest = objectMapper.writeValueAsString(request);

        // We must mock the User and Account because the controller looks up the Principal
        User mockUser = new User();
        mockUser.setEmail("alicejohnson@example.com");
        Account mockAccount = new Account();
        mockUser.setAccount(mockAccount);

        // Program our mock beans
        when(userRepository.findByEmail("alicejohnson@example.com")).thenReturn(Optional.of(mockUser));
        when(transactionService.depositFunds(any(Account.class), any(DepositRequestDto.class)))
                .thenReturn("Successfully deposited 5000.00");

        // 2. ACT & 3. ASSERT
        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully deposited 5000.00"));
    }

    @Test
    void depositFunds_withoutAuthentication_returnsUnauthorized() throws Exception {
        // 1. ARRANGE
        DepositRequestDto request = new DepositRequestDto();
        request.setAmount(new BigDecimal("5000.00"));

        String jsonRequest = objectMapper.writeValueAsString(request);

        // 2. ACT & 3. ASSERT
        // Notice there is NO @WithMockUser on this test method.
        // We expect Spring Security to intercept and block it with a 401 Unauthorized or 403 Forbidden.
        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
        // Note: Depending on your exact security
    }
}