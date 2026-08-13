package com.wallet.core.service;

import com.wallet.core.entity.Account;
import com.wallet.core.entity.User;
import com.wallet.core.exception.AccountNotFoundException;
import com.wallet.core.repository.AccountRepository;
import com.wallet.core.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final UserRepository userRepository;

    public BigDecimal getMyBalance(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("User not found"));

        return user.getAccount().getBalance();
    }

    public Account getAccountByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("User Not found"));
        return user.getAccount();
    }
}
