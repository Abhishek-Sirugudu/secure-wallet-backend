package com.wallet.core.service;

import com.wallet.core.dto.LoginRequestDto;
import com.wallet.core.dto.RegisterRequestDto;
import com.wallet.core.entity.Account;
import com.wallet.core.entity.User;
import com.wallet.core.enums.AccountStatus;
import com.wallet.core.exception.DuplicateEmailException;
import com.wallet.core.exception.InvalidCredentialsException;
import com.wallet.core.repository.AccountRepository;
import com.wallet.core.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Transactional
    public User registerUser(RegisterRequestDto request){

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new DuplicateEmailException("Email already exists");
        }

        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        Account newAccount = Account.builder()
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.valueOf(1000))
                .status(AccountStatus.ACTIVE)
                .dailyLimit(new BigDecimal("50000.00"))
                .user(newUser)
                .build();

        newUser.setAccount(newAccount);

        return userRepository.save(newUser);
    }
    private String generateAccountNumber() {
        return String.valueOf((long) (Math.random() * 10000000000L));
    }

    public String Login(LoginRequestDto request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPasswordHash())){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return jwtService.generateToken(user);
    }
}
