package bank.card.management.controller;

import bank.card.management.dto.request.CreateCardRequest;
import bank.card.management.dto.response.CardResponse;
import bank.card.management.service.CardService;
import bank.card.management.util.PageRequestHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
@RequiredArgsConstructor
public class CardController {
    
    private final CardService cardService;
    
    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @Valid @RequestBody CreateCardRequest request,
            Authentication authentication) {
        CardResponse card = cardService.createCard(request, authentication.getName());
        return ResponseEntity.ok(card);
    }
    
    @GetMapping("/my")
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        
        Pageable pageable = PageRequestHelper.createPageable(page, size, sortBy);
        
        Page<CardResponse> cards;
        if (search != null && !search.trim().isEmpty()) {
            cards = cardService.searchUserCards(authentication.getName(), search, pageable);
        } else {
            cards = cardService.getUserCards(authentication.getName(), pageable);
        }
        
        return ResponseEntity.ok(cards);
    }
    
    @GetMapping("/my/{id}")
    public ResponseEntity<CardResponse> getMyCard(
            @PathVariable Long id,
            Authentication authentication) {
        CardResponse card = cardService.getCardById(id, authentication.getName());
        return ResponseEntity.ok(card);
    }
    
    @PostMapping("/my/{id}/block")
    public ResponseEntity<CardResponse> blockMyCard(
            @PathVariable Long id,
            Authentication authentication) {
        CardResponse card = cardService.blockCard(id, authentication.getName());
        return ResponseEntity.ok(card);
    }
}

