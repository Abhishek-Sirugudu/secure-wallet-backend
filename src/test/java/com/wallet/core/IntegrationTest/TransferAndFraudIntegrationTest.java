package com.wallet.core.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.core.dto.TransferRequestDto;
import com.wallet.core.entity.Account;
import com.wallet.core.entity.FraudFlag;
import com.wallet.core.entity.User;
import com.wallet.core.enums.AccountStatus;
import com.wallet.core.enums.FraudReviewStatus;
import com.wallet.core.repository.AccountRepository;
import com.wallet.core.repository.FraudFlagRepository;
import com.wallet.core.repository.TransactionRepository;
import com.wallet.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Uses the H2 in-memory database
class TransferAndFraudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FraudFlagRepository fraudFlagRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        fraudFlagRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Sender (Balance: 100,000 | Daily Limit: 50,000)
        User sender = User.builder().name("Alice").email("alice@test.com").passwordHash("hash").role("USER").build();
        senderAccount = Account.builder().user(sender).accountNumber("SENDER123").balance(new BigDecimal("100000.00")).status(AccountStatus.ACTIVE).dailyLimit(new BigDecimal("50000.00")).build();
        sender.setAccount(senderAccount);
        userRepository.save(sender);

        // 2. Create Receiver (Balance: 0 | Daily Limit: 50,000)
        User receiver = User.builder().name("Bob").email("bob@test.com").passwordHash("hash").role("USER").build();
        receiverAccount = Account.builder().user(receiver).accountNumber("RECEIVER456").balance(new BigDecimal("0.00")).status(AccountStatus.ACTIVE).dailyLimit(new BigDecimal("50000.00")).build();
        receiver.setAccount(receiverAccount);
        userRepository.save(receiver);
    }

    @Test
    @WithMockUser(username = "alice@test.com", roles = "USER")
    void endToEnd_fraudFlag_and_adminReview_flow() throws Exception {

        // ==========================================
        // ACT 1: SENDER ATTEMPTS A FRAUDULENT TRANSFER
        // ==========================================

        // Alice tries to send 60,000 (which is OVER her 50,000 daily limit!)
        TransferRequestDto transferRequest = new TransferRequestDto();
        transferRequest.setReceiverAccountNumber("RECEIVER456");
        transferRequest.setAmount(new BigDecimal("60000.00"));

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk()); // Controller returns 200 OK, but with a flagged message

        // ==========================================
        // ASSERT 1: VERIFY FUNDS ARE HELD IN LIMBO
        // ==========================================

        // Fetch fresh account data from the database
        Account updatedSender = accountRepository.findByAccountNumber("SENDER123").get();
        Account updatedReceiver = accountRepository.findByAccountNumber("RECEIVER456").get();

        // Sender MUST be debited (100k - 60k = 40k)
        assertEquals(0, new BigDecimal("40000.00").compareTo(updatedSender.getBalance()));

        // Receiver MUST NOT be credited yet (Still 0)
        assertEquals(0, new BigDecimal("0.00").compareTo(updatedReceiver.getBalance()));

        // Verify a FraudFlag was created
        List<FraudFlag> flags = fraudFlagRepository.findAll();
        assertEquals(1, flags.size());
        assertEquals(FraudReviewStatus.PENDING, flags.get(0).getReviewStatus());

        Long flagId = flags.get(0).getId();

        // ==========================================
        // ACT 2: ADMIN REVIEWS AND CLEARS THE FRAUD
        // ==========================================

        // We use a JSON string directly here for simplicity
        String reviewPayload = "{\"decision\": \"CLEARED\"}";

        mockMvc.perform(put("/api/fraud/flags/" + flagId + "/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload)) // Wait! We didn't authenticate as Admin! Let's see what happens...
                .andExpect(status().isForbidden()); // SUCCESS! Our security patch blocked Alice from clearing her own fraud!

        // Now, let's actually login as the Admin and do it properly:
        mockMvc.perform(put("/api/fraud/flags/" + flagId + "/review")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@wallet.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewPayload))
                .andExpect(status().isOk()); // Admin succeeds!

        // ==========================================
        // ASSERT 2: VERIFY FUNDS ARE RELEASED TO RECEIVER
        // ==========================================

        updatedSender = accountRepository.findByAccountNumber("SENDER123").get();
        updatedReceiver = accountRepository.findByAccountNumber("RECEIVER456").get();

        // Sender balance stays at 40k
        assertEquals(0, new BigDecimal("40000.00").compareTo(updatedSender.getBalance()));

        // Receiver FINALLY gets the money (0 -> 60k)
        assertEquals(0, new BigDecimal("60000.00").compareTo(updatedReceiver.getBalance()));

        // Verify flag is cleared
        FraudFlag updatedFlag = fraudFlagRepository.findById(flagId).get();
        assertEquals(FraudReviewStatus.CLEARED, updatedFlag.getReviewStatus());

    }
}