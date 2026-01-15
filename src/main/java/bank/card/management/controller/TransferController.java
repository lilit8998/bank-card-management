package bank.card.management.controller;

import bank.card.management.dto.request.TransferRequest;
import bank.card.management.dto.response.TransferResponse;
import bank.card.management.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
@RequiredArgsConstructor
public class TransferController {
    
    private final TransferService transferService;
    
    @PostMapping
    public ResponseEntity<TransferResponse> transferBetweenCards(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        TransferResponse response = transferService.transferBetweenOwnCards(request, authentication.getName());
        return ResponseEntity.ok(response);
    }
}

