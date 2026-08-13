package com.wallet.core.fraud;

import com.wallet.core.enums.FraudSeverity;

public record FraudCheckResult(boolean isFlagged, String reason, FraudSeverity severity) {
    public static FraudCheckResult clean() {
        return new FraudCheckResult(false, null, null);
    }

    public static FraudCheckResult flagged(String reason, FraudSeverity severity) {
        return new FraudCheckResult(true, reason, severity);
    }
}
