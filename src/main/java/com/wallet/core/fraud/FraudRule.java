package com.wallet.core.fraud;

import com.wallet.core.entity.Transaction;

public interface FraudRule {
    FraudCheckResult evaluate(Transaction transaction);
}
