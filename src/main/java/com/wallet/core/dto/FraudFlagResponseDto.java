package com.wallet.core.dto;

import com.wallet.core.entity.FraudFlag;
import com.wallet.core.enums.FraudReviewStatus;
import com.wallet.core.enums.FraudSeverity;
import java.time.LocalDateTime;

public record FraudFlagResponseDto(
        Long id, Long transactionId, String reason, FraudSeverity severity,
        FraudReviewStatus reviewStatus, String reviewedBy, LocalDateTime createdAt) {

    public static FraudFlagResponseDto from(FraudFlag flag) {
        return new FraudFlagResponseDto(
                flag.getId(), flag.getTransaction().getId(), flag.getReason(),
                flag.getSeverity(), flag.getReviewStatus(), flag.getReviewedBy(), flag.getCreatedAt());
    }
}