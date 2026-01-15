package bank.card.management.util;

import bank.card.management.exception.JwtTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {
    
    @InjectMocks
    private JwtUtil jwtUtil;
    
    private UserDetails userDetails;
    private String secret = "test-secret-key-for-jwt-token-generation-2024-minimum-256-bits-required-for-hmac-sha";
    private Long expiration = 86400000L; // 24 hours
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("password")
                .authorities(Arrays.asList(
                        new SimpleGrantedAuthority(RoleConstants.ROLE_PREFIX + RoleConstants.USER)
                ))
                .disabled(false)
                .build();
    }
    
    @Test
    void testGenerateToken_Success() {
        String token = jwtUtil.generateToken(userDetails);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testExtractUsername_Success() {
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        
        assertEquals("testuser", username);
    }
    
    @Test
    void testExtractExpiration_Success() {
        String token = jwtUtil.generateToken(userDetails);
        Date expiration = jwtUtil.extractExpiration(token);
        
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
    
    @Test
    void testValidateToken_ValidToken() {
        String token = jwtUtil.generateToken(userDetails);
        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        
        assertTrue(isValid);
    }
    
    @Test
    void testValidateToken_InvalidUsername() {
        String token = jwtUtil.generateToken(userDetails);
        
        UserDetails differentUser = org.springframework.security.core.userdetails.User.builder()
                .username("differentuser")
                .password("password")
                .authorities(Arrays.asList(new SimpleGrantedAuthority(RoleConstants.ROLE_PREFIX + RoleConstants.USER)))
                .disabled(false)
                .build();
        
        Boolean isValid = jwtUtil.validateToken(token, differentUser);
        
        assertFalse(isValid);
    }
    
    @Test
    void testValidateToken_ExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        
        String token = jwtUtil.generateToken(userDetails);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);

        // When token is expired, extractUsername throws JwtTokenException
        JwtTokenException exception = assertThrows(JwtTokenException.class, 
            () -> jwtUtil.validateToken(token, userDetails));
        
        assertTrue(exception.getCause() instanceof ExpiredJwtException || 
                  exception.getMessage().contains("expired") ||
                  exception.getMessage().contains("JWT token has expired"));
    }
    
    @Test
    void testExtractUsername_InvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(JwtTokenException.class, () -> jwtUtil.extractUsername(invalidToken));
    }
    
    @Test
    void testGenerateToken_WithAuthorities() {
        UserDetails userWithMultipleRoles = org.springframework.security.core.userdetails.User.builder()
                .username("admin")
                .password("password")
                .authorities(Arrays.asList(
                        new SimpleGrantedAuthority(RoleConstants.ROLE_PREFIX + RoleConstants.ADMIN),
                        new SimpleGrantedAuthority(RoleConstants.ROLE_PREFIX + RoleConstants.USER)
                ))
                .disabled(false)
                .build();
        
        String token = jwtUtil.generateToken(userWithMultipleRoles);
        
        assertNotNull(token);
        String username = jwtUtil.extractUsername(token);
        assertEquals("admin", username);
    }
}
