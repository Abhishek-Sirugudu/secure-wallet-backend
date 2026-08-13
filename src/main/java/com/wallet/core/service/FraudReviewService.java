package com.wallet.core.service;

import com.wallet.core.entity.Account;
import com.wallet.core.entity.FraudFlag;
import com.wallet.core.entity.Transaction;
import com.wallet.core.enums.FraudReviewStatus;
import com.wallet.core.enums.TransactionStatus;
import com.wallet.core.exception.FlagAlreadyReviewedException;
import com.wallet.core.exception.FraudFlagNotFoundException;
import com.wallet.core.repository.FraudFlagRepository;
import com.wallet.core.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudReviewService {

    private final FraudFlagRepository fraudFlagRepository;
    private final TransactionRepository transactionRepository;

    public List<FraudFlag> getPendingFlags() {
        return fraudFlagRepository.findByReviewStatus(FraudReviewStatus.PENDING);
    }

    @Transactional
    public String reviewFlag(Long flagId, FraudReviewStatus decision, String adminEmail) {
        FraudFlag flag = fraudFlagRepository.findById(flagId)
                .orElseThrow(() -> new FraudFlagNotFoundException("Fraud flag not found"));

        if (flag.getReviewStatus() != FraudReviewStatus.PENDING) {
            throw new FlagAlreadyReviewedException("This flag has already been reviewed");
        }

        Transaction transaction = flag.getTransaction();
        Account sender = transaction.getSenderAccount();
        Account receiver = transaction.getReceiverAccount();

        flag.setReviewStatus(decision);
        flag.setReviewedBy(adminEmail);

        if (decision == FraudReviewStatus.CLEARED) {
            // FRAUD CLEARED: Complete the transfer by crediting the receiver
            transaction.setStatus(TransactionStatus.SUCCESS);
            receiver.setBalance(receiver.getBalance().add(transaction.getAmount()));

        } else if (decision == FraudReviewStatus.CONFIRMED_FRAUD) {
            // FRAUD CONFIRMED: Cancel the transfer and refund the sender
            transaction.setStatus(TransactionStatus.FAILED);
            sender.setBalance(sender.getBalance().add(transaction.getAmount()));

            // Note: In a future iteration, we could freeze the sender's account here
        }

        fraudFlagRepository.save(flag);
        transactionRepository.save(transaction);

        return "Transaction " + transaction.getId() + " has been " + decision;
    }
}