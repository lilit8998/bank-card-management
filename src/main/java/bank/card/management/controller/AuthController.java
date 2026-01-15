package bank.card.management.controller;

import bank.card.management.dto.request.LoginRequest;
import bank.card.management.dto.request.RegisterRequest;
import bank.card.management.dto.response.JwtResponse;
import bank.card.management.dto.response.UserResponse;
import bank.card.management.mapper.UserMapper;
import bank.card.management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final UserMapper userMapper;
    
    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }
    
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        return ResponseEntity.ok(userMapper.toUserResponse(authService.registerUser(signUpRequest)));
    }
}

