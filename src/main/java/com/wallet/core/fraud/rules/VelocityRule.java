package com.wallet.core.fraud.rules;

import com.wallet.core.entity.Transaction;
import com.wallet.core.enums.FraudSeverity;
import com.wallet.core.fraud.FraudCheckResult;
import com.wallet.core.fraud.FraudRule;
import com.wallet.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VelocityRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    // If a user attempts more than 5 transfers in 10 minutes,we will flag it.
    private static final int MAX_TRANSFERS = 5;
    private static final int TIME_WINDOW_MINUTES = 10;

    @Override
    public FraudCheckResult evaluate(Transaction transaction) {
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(TIME_WINDOW_MINUTES);

        int recentTransfers = transactionRepository.countBySenderAccountIdAndTimestampAfter(
                transaction.getSenderAccount().getId(),
                thresholdTime
        );

        if (recentTransfers >= MAX_TRANSFERS) {
            return FraudCheckResult.flagged(
                    "High velocity detected: Exceeded " + MAX_TRANSFERS + " transfers in the last " + TIME_WINDOW_MINUTES + " minutes.",
                    FraudSeverity.HIGH
            );
        }

        return FraudCheckResult.clean();
    }
}