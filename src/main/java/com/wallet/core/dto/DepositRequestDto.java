package com.wallet.core.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequestDto {
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "1.00", message = "Deposit must be greater than zero")
    private BigDecimal amount;

}
