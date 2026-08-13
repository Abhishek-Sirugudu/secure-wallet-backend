package com.wallet.core.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDto {

    @NotBlank(message = "Receiver account number is required")
    private String receiverAccountNumber;

    @NotNull
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero ")
    private BigDecimal amount;
}
