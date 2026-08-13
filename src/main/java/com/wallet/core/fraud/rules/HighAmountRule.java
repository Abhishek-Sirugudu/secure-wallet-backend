package com.wallet.core.fraud.rules;

import com.wallet.core.entity.Transaction;
import com.wallet.core.enums.FraudSeverity;
import com.wallet.core.fraud.FraudCheckResult;
import com.wallet.core.fraud.FraudRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements FraudRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("50000.00");

    @Override
    public FraudCheckResult evaluate(Transaction transaction) {
        if (transaction.getAmount().compareTo(THRESHOLD) > 0) {
            return FraudCheckResult.flagged("Transferred amount exceeds threshold of " + THRESHOLD, FraudSeverity.HIGH);
        }
        return FraudCheckResult.clean();
    }
}
