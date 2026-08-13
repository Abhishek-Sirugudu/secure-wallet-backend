package com.wallet.core.config;

import com.wallet.core.entity.Account;
import com.wallet.core.entity.User;
import com.wallet.core.enums.AccountStatus;
import com.wallet.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import org.slf4j.Logger;


@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only create the admin if it doesn't already exist
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {

            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@example.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role("ROLE_ADMIN")
                    .build();

            Account adminAccount = Account.builder()
                    .accountNumber("0000000000")
                    .balance(BigDecimal.ZERO)
                    .status(AccountStatus.ACTIVE)
                    .dailyLimit(new BigDecimal("1000000.00"))
                    .user(admin)
                    .build();

            admin.setAccount(adminAccount);
            userRepository.save(admin);

            Logger log = LoggerFactory.getLogger(AdminSeeder.class);
            log.info("Default admin created");
        }
    }
}