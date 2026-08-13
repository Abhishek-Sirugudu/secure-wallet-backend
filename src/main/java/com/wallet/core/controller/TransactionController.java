package com.wallet.core.controller;

import com.wallet.core.dto.DepositRequestDto;
import com.wallet.core.dto.TransferRequestDto;
import com.wallet.core.entity.Account;
import com.wallet.core.entity.Transaction;
import com.wallet.core.entity.User;
import com.wallet.core.exception.AccountNotFoundException;
import com.wallet.core.repository.UserRepository;
import com.wallet.core.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping("/transfer")
    public ResponseEntity<String> transferFunds(@Valid @RequestBody TransferRequestDto request, Principal principal) {

        String email = principal.getName();

        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("User not found"));
        Account senderAccount = sender.getAccount();

        String result = transactionService.transferFunds(senderAccount, request);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> depositFunds(
            @Valid @RequestBody DepositRequestDto request,
            Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String result = transactionService.depositFunds(user.getAccount(), request);

        return ResponseEntity.ok(result);
    }
    @GetMapping("/history")
    public ResponseEntity<List<Transaction>> getTransactionHistory(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getAccount() == null) {
            throw new RuntimeException("Account not found for user");
        }

        List<Transaction> history = transactionService.getTransactionHistory(user.getAccount().getId());
        return ResponseEntity.ok(history);
    }
}