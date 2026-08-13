package com.wallet.core.service;

import com.wallet.core.dto.DepositRequestDto;
import com.wallet.core.dto.TransferRequestDto;
import com.wallet.core.entity.Account;
import com.wallet.core.entity.FraudFlag;
import com.wallet.core.entity.Transaction;
import com.wallet.core.enums.FraudReviewStatus;
import com.wallet.core.enums.TransactionStatus;
import com.wallet.core.enums.TransactionType;
import com.wallet.core.exception.AccountNotFoundException;
import com.wallet.core.exception.InsufficientFundsException;
import com.wallet.core.exception.SelfTransferException;
import com.wallet.core.fraud.FraudCheckResult;
import com.wallet.core.fraud.FraudDetectionEngine;
import com.wallet.core.repository.AccountRepository;
import com.wallet.core.repository.FraudFlagRepository;
import com.wallet.core.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionEngine fraudDetectionEngine;
    private final FraudFlagRepository fraudFlagRepository;

    @Transactional
    public String depositFunds(Account unLockedAccount, DepositRequestDto request) {


        Account lockedAccount = accountRepository.findByIdForUpdate(unLockedAccount.getId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        lockedAccount.setBalance(lockedAccount.getBalance().add(request.getAmount()));

        Transaction transaction = Transaction.builder()
                .senderAccount(lockedAccount)
                .receiverAccount(lockedAccount)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .build();

        transactionRepository.save(transaction);
        return "Successfully deposited " + request.getAmount();
    }

    @Transactional
    public String transferFunds(Account unLockedSenderAccount, TransferRequestDto request) {


        if (unLockedSenderAccount.getAccountNumber().equals(request.getReceiverAccountNumber())) {
            throw new SelfTransferException("Cannot transfer funds to your own account");
        }
        Account receiverPreview = accountRepository.findByAccountNumber(request.getReceiverAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));
        Account senderAccount;
        Account receiverAccount;

        if (unLockedSenderAccount.getId() < receiverPreview.getId()) {
            senderAccount = accountRepository.findByIdForUpdate(unLockedSenderAccount.getId())
                    .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));
            receiverAccount = accountRepository.findByIdForUpdate(receiverPreview.getId())
                    .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));
        } else {
            receiverAccount = accountRepository.findByIdForUpdate(receiverPreview.getId())
                    .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));
            senderAccount = accountRepository.findByIdForUpdate(unLockedSenderAccount.getId())
                    .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));
        }

        if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        Transaction transaction = Transaction.builder()
                .senderAccount(senderAccount)
                .receiverAccount(receiverAccount)
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .build();


        FraudCheckResult fraudResult = fraudDetectionEngine.runChecks(transaction);

        if (fraudResult.isFlagged()) {
            transaction.setStatus(TransactionStatus.FLAGGED);

            // Only debit the sender
            senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
            transactionRepository.save(transaction);

            FraudFlag flag = FraudFlag.builder()
                    .transaction(transaction)
                    .reason(fraudResult.reason())
                    .severity(fraudResult.severity())
                    .reviewStatus(FraudReviewStatus.PENDING)
                    .build();
            fraudFlagRepository.save(flag);

            return "Transfer flagged for manual review.";
        }

        // 4. Clean Transfer
        transaction.setStatus(TransactionStatus.SUCCESS);
        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));
        transactionRepository.save(transaction);

        return "Transfer successful!";
    }
    public List<Transaction> getTransactionHistory(Long accountId) {
        return transactionRepository.findAllByAccountId(accountId);
    }
}