package bank.card.management.service;

import bank.card.management.dto.request.LoginRequest;
import bank.card.management.dto.request.RegisterRequest;
import bank.card.management.dto.response.JwtResponse;
import bank.card.management.entity.User;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);

    User registerUser(RegisterRequest registerRequest);
}
