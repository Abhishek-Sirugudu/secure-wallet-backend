package com.wallet.core.dto;

import com.wallet.core.enums.FraudReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FraudReviewRequestDto {
    @NotNull(message = "Review status cannot be null")
    private FraudReviewStatus decision;
}
