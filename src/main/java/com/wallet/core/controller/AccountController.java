package com.wallet.core.controller;

import com.wallet.core.entity.Account;
import com.wallet.core.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<BigDecimal> getMyBalance(Principal principal){
        BigDecimal balance = accountService.getMyBalance(principal.getName());

        return ResponseEntity.ok(balance);
    }
    @GetMapping("/details")
    public ResponseEntity<Account> getMyAccountDetails(Principal principal) {
        // Assuming your accountService can fetch the User/Account by email!
        // You might need to add getAccountByEmail to your AccountService if it doesn't exist.
        Account account = accountService.getAccountByEmail(principal.getName());

        return ResponseEntity.ok(account);
    }

}
