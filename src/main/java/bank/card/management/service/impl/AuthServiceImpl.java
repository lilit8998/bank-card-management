package bank.card.management.service.impl;

import bank.card.management.dto.request.LoginRequest;
import bank.card.management.dto.request.RegisterRequest;
import bank.card.management.dto.response.JwtResponse;
import bank.card.management.entity.Role;
import bank.card.management.entity.User;
import bank.card.management.exception.EmailAlreadyInUseException;
import bank.card.management.exception.RoleNotFoundException;
import bank.card.management.exception.UserNotFoundException;
import bank.card.management.exception.UsernameAlreadyTakenException;
import bank.card.management.repository.RoleRepository;
import bank.card.management.repository.UserRepository;
import bank.card.management.service.AuthService;
import bank.card.management.util.JwtConstants;
import bank.card.management.util.JwtUtil;
import bank.card.management.util.RoleConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    
    @Override
    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + loginRequest.getUsername()));
        
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        
        return new JwtResponse(jwt, JwtConstants.BEARER_TYPE, user.getId(), user.getUsername(), user.getEmail(), roles);
    }
    
    @Override
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyTakenException(registerRequest.getUsername());
        }
        
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyInUseException(registerRequest.getEmail());
        }
        
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(Role.RoleName.USER)
                .orElseThrow(() -> new RoleNotFoundException(RoleConstants.USER));
        roles.add(userRole);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }
}
