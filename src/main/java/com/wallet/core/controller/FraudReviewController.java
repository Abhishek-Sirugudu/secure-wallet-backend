package com.wallet.core.controller;

import com.wallet.core.dto.FraudFlagResponseDto;
import com.wallet.core.dto.FraudReviewRequestDto;
import com.wallet.core.service.FraudReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FraudReviewController {

    private final FraudReviewService fraudReviewService;

    @GetMapping("/flags")
    public ResponseEntity<List<FraudFlagResponseDto>> getPendingFlags() {
        List<FraudFlagResponseDto> flags = fraudReviewService.getPendingFlags().stream()
                .map(FraudFlagResponseDto::from)
                .toList();
        return ResponseEntity.ok(flags);
    }

    @PutMapping("/flags/{id}/review")
    public ResponseEntity<String> reviewFlag(
            @PathVariable Long id,
            @Valid @RequestBody FraudReviewRequestDto request,
            Principal principal) {

        String result = fraudReviewService.reviewFlag(id, request.getDecision(), principal.getName());
        return ResponseEntity.ok(result);
    }
}