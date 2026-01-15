package bank.card.management.service;

import bank.card.management.dto.request.LoginRequest;
import bank.card.management.dto.request.RegisterRequest;
import bank.card.management.dto.response.JwtResponse;
import bank.card.management.entity.Role;
import bank.card.management.entity.User;
import bank.card.management.exception.EmailAlreadyInUseException;
import bank.card.management.exception.RoleNotFoundException;
import bank.card.management.exception.UsernameAlreadyTakenException;
import bank.card.management.repository.RoleRepository;
import bank.card.management.repository.UserRepository;
import bank.card.management.service.impl.AuthServiceImpl;
import bank.card.management.service.impl.UserDetailsServiceImpl;
import bank.card.management.util.JwtConstants;
import bank.card.management.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private UserDetailsServiceImpl userDetailsService;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    private User testUser;
    private Role userRole;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    
    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRoles(roles);
        
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
    }
    
    @Test
    void testAuthenticateUser_Success() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        JwtResponse result = authService.authenticateUser(loginRequest);
        
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals(JwtConstants.BEARER_TYPE, result.getType());
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertNotNull(result.getRoles());
        
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }
    
    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        User result = authService.registerUser(registerRequest);
        
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }
    
    @Test
    void testRegisterUser_UsernameAlreadyTaken() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);
        
        assertThrows(UsernameAlreadyTakenException.class, 
            () -> authService.registerUser(registerRequest));
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testRegisterUser_EmailAlreadyInUse() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);
        
        assertThrows(EmailAlreadyInUseException.class, 
            () -> authService.registerUser(registerRequest));
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testRegisterUser_RoleNotFound() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.empty());
        
        assertThrows(RoleNotFoundException.class, 
            () -> authService.registerUser(registerRequest));
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testRegisterUser_PasswordEncoded() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals("encodedPassword", savedUser.getPassword());
            return savedUser;
        });
        
        authService.registerUser(registerRequest);
        
        verify(passwordEncoder, times(1)).encode("password123");
    }
    
    @Test
    void testRegisterUser_UserRoleAssigned() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertNotNull(savedUser.getRoles());
            assertTrue(savedUser.getRoles().contains(userRole));
            return savedUser;
        });
        
        authService.registerUser(registerRequest);
        
        verify(roleRepository, times(1)).findByName(Role.RoleName.USER);
    }
}
