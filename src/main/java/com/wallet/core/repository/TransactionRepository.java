package com.wallet.core.repository;

import com.wallet.core.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    // For the Velocity Rule
    int countBySenderAccountIdAndTimestampAfter(Long accountId, LocalDateTime timestamp);

    // For the Daily Limit Rule
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.senderAccount.id = :accountId AND t.timestamp >= :startOfDay")
    BigDecimal sumAmountBySenderAccountIdAndTimestampAfter(@Param("accountId") Long accountId, @Param("startOfDay") LocalDateTime startOfDay);

    // Fetch transaction history (both sent and received) ordered by newest first
    @Query("SELECT t FROM Transaction t WHERE t.senderAccount.id = :accountId OR t.receiverAccount.id = :accountId ORDER BY t.timestamp DESC")
    List<Transaction> findAllByAccountId(@Param("accountId") Long accountId);
}
