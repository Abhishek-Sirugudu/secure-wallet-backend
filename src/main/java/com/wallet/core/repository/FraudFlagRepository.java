package com.wallet.core.repository;

import com.wallet.core.entity.FraudFlag;
import com.wallet.core.enums.FraudReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudFlagRepository extends JpaRepository<FraudFlag,Long> {
    List<FraudFlag> findByReviewStatus(FraudReviewStatus status);
}
