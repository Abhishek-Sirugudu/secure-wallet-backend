package com.wallet.core.fraud.rules;

import com.wallet.core.entity.Transaction;
import com.wallet.core.enums.FraudSeverity;
import com.wallet.core.fraud.FraudCheckResult;
import com.wallet.core.fraud.FraudRule;
import com.wallet.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DailyLimitRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public FraudCheckResult evaluate(Transaction transaction) {
        BigDecimal dailyLimit = transaction.getSenderAccount().getDailyLimit();

        // Safety check just in case an account doesn't have a limit set
        if (dailyLimit == null) {
            return FraudCheckResult.clean();
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        BigDecimal spentToday = transactionRepository.sumAmountBySenderAccountIdAndTimestampAfter(
                transaction.getSenderAccount().getId(),
                startOfDay
        );

        BigDecimal totalAttempted = spentToday.add(transaction.getAmount());

        if (totalAttempted.compareTo(dailyLimit) > 0) {
            return FraudCheckResult.flagged(
                    "Daily transfer limit of " + dailyLimit + " exceeded. Attempted total for today: " + totalAttempted,
                    FraudSeverity.MEDIUM
            );
        }

        return FraudCheckResult.clean();
    }
}