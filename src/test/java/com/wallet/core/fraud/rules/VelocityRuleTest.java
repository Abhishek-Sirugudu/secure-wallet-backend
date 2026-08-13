package com.wallet.core.fraud.rules;

import com.wallet.core.entity.Account;
import com.wallet.core.entity.Transaction;
import com.wallet.core.enums.FraudSeverity;
import com.wallet.core.fraud.FraudCheckResult;
import com.wallet.core.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Tells JUnit to enable Mockito
class VelocityRuleTest {

    @Mock
    private TransactionRepository transactionRepository; // Our fake database

    @InjectMocks
    private VelocityRule velocityRule; // The real rule we are testing, injected with the fake DB

    @Test
    void evaluate_whenUnderTransferLimit_returnsClean() {
        // 1. ARRANGE (Set up the fake scenario)
        Account sender = new Account();
        sender.setId(1L);

        Transaction transaction = new Transaction();
        transaction.setSenderAccount(sender);

        // Program the fake database: "If asked how many recent transfers user 1 made, say 2."
        when(transactionRepository.countBySenderAccountIdAndTimestampAfter(eq(1L), any()))
                .thenReturn(2);

        // 2. ACT (Run the method being tested)
        FraudCheckResult result = velocityRule.evaluate(transaction);

        // 3. ASSERT (Verify the results)
        assertFalse(result.isFlagged());
        assertNull(result.reason());
    }

    @Test
    void evaluate_whenAtOrOverTransferLimit_returnsFlagged() {
        // 1. ARRANGE
        Account sender = new Account();
        sender.setId(1L);

        Transaction transaction = new Transaction();
        transaction.setSenderAccount(sender);

        // Program the fake database: "Say they've already made 5 transfers (our max limit)."
        when(transactionRepository.countBySenderAccountIdAndTimestampAfter(eq(1L), any()))
                .thenReturn(5);

        // 2. ACT
        FraudCheckResult result = velocityRule.evaluate(transaction);

        // 3. ASSERT
        assertTrue(result.isFlagged());
        assertEquals(FraudSeverity.HIGH, result.severity());
        assertTrue(result.reason().contains("High velocity detected"));
    }
}