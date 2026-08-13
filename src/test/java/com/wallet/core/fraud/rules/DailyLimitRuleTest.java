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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyLimitRuleTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DailyLimitRule dailyLimitRule;

    @Test
    void evaluate_whenNoDailyLimitSet_returnsClean() {
        // 1. ARRANGE
        Account sender = new Account();
        sender.setDailyLimit(null); // No limit set on the account

        Transaction transaction = new Transaction();
        transaction.setSenderAccount(sender);

        // 2. ACT
        FraudCheckResult result = dailyLimitRule.evaluate(transaction);

        // 3. ASSERT
        assertFalse(result.isFlagged());
    }

    @Test
    void evaluate_whenUnderDailyLimit_returnsClean() {
        // 1. ARRANGE
        Account sender = new Account();
        sender.setId(1L);
        sender.setDailyLimit(new BigDecimal("50000.00"));

        Transaction transaction = new Transaction();
        transaction.setSenderAccount(sender);
        transaction.setAmount(new BigDecimal("10000.00")); // Trying to send 10,000

        //  User has already spent 20,000 today
        when(transactionRepository.sumAmountBySenderAccountIdAndTimestampAfter(eq(1L), any()))
                .thenReturn(new BigDecimal("20000.00"));

        // 2. ACT
        FraudCheckResult result = dailyLimitRule.evaluate(transaction);

        // 3. ASSERT
        // 20,000 + 10,000 = 30,000 (which is less than the 50,000 limit)
        assertFalse(result.isFlagged());
    }

    @Test
    void evaluate_whenExceedsDailyLimit_returnsFlagged() {
        // 1. ARRANGE
        Account sender = new Account();
        sender.setId(1L);
        sender.setDailyLimit(new BigDecimal("50000.00"));

        Transaction transaction = new Transaction();
        transaction.setSenderAccount(sender);
        transaction.setAmount(new BigDecimal("15000.00"));

        when(transactionRepository.sumAmountBySenderAccountIdAndTimestampAfter(eq(1L), any()))
                .thenReturn(new BigDecimal("40000.00"));


        FraudCheckResult result = dailyLimitRule.evaluate(transaction);

        assertTrue(result.isFlagged());
        assertEquals(FraudSeverity.MEDIUM, result.severity());
        assertTrue(result.reason().contains("Daily transfer limit of 50000.00 exceeded"));
    }
}