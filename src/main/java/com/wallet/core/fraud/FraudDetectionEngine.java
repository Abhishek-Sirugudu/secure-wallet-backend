package com.wallet.core.fraud;

import com.wallet.core.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudDetectionEngine {

    private final List<FraudRule> rules;

    public FraudCheckResult runChecks(Transaction transaction){
        for(FraudRule rule : rules){
            FraudCheckResult result = rule.evaluate(transaction);

            if (result.isFlagged()) {
                return result;
            }
        }
        return FraudCheckResult.clean();
    }

}
