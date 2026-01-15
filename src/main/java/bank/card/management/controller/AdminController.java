package bank.card.management.controller;

import bank.card.management.dto.request.CreateCardRequest;
import bank.card.management.dto.response.AdminCardResponse;
import bank.card.management.dto.response.CardResponse;
import bank.card.management.dto.response.UserResponse;
import bank.card.management.service.CardService;
import bank.card.management.service.UserService;
import bank.card.management.util.PageRequestHelper;
import bank.card.management.util.RoleConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
@RequiredArgsConstructor
public class AdminController {
    
    private final CardService cardService;
    private final UserService userService;
    
    @PostMapping("/cards")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<CardResponse> createCard(
            @Valid @RequestBody CreateCardRequest request,
            Authentication authentication) {
        CardResponse card = cardService.createCard(request, authentication.getName());
        return ResponseEntity.ok(card);
    }
    
    @GetMapping("/cards")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<Page<AdminCardResponse>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        Pageable pageable = PageRequestHelper.createPageable(page, size, sortBy);
        Page<AdminCardResponse> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }
    
    @GetMapping("/cards/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<AdminCardResponse> getCard(@PathVariable Long id) {
        AdminCardResponse card = cardService.getCardByIdAdmin(id);
        return ResponseEntity.ok(card);
    }
    
    @PostMapping("/cards/{id}/activate")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<AdminCardResponse> activateCard(@PathVariable Long id) {
        AdminCardResponse card = cardService.activateCard(id);
        return ResponseEntity.ok(card);
    }
    
    @PostMapping("/cards/{id}/block")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<AdminCardResponse> blockCard(@PathVariable Long id) {
        AdminCardResponse card = cardService.blockCardAdmin(id);
        return ResponseEntity.ok(card);
    }
    
    @DeleteMapping("/cards/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
    
    // User Management
    @GetMapping("/users")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        Pageable pageable = PageRequestHelper.createPageable(page, size, sortBy);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/users/{id}/block")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<UserResponse> blockUser(@PathVariable Long id) {
        UserResponse user = userService.blockUser(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/users/{id}/unblock")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public ResponseEntity<UserResponse> unblockUser(@PathVariable Long id) {
        UserResponse user = userService.unblockUser(id);
        return ResponseEntity.ok(user);
    }
}

