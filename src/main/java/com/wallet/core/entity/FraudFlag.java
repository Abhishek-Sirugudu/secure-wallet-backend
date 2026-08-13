package com.wallet.core.entity;

import com.wallet.core.enums.FraudReviewStatus;
import com.wallet.core.enums.FraudSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_flags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FraudFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FraudSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudReviewStatus reviewStatus;

    // This will store the email or ID of the admin who reviewed it
    private String reviewedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}