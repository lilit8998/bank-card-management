package bank.card.management.security;

import bank.card.management.service.impl.UserDetailsServiceImpl;
import bank.card.management.util.JwtConstants;
import bank.card.management.util.JwtUtil;
import bank.card.management.util.RoleConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    
    @Mock
    private UserDetailsServiceImpl userDetailsService;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    private UserDetails userDetails;
    private String validToken = "valid-jwt-token";
    private String username = "testuser";
    
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("password")
                .authorities(Arrays.asList(new SimpleGrantedAuthority(RoleConstants.ROLE_PREFIX + RoleConstants.USER)))
                .disabled(false)
                .build();
    }
    
    @Test
    void testDoFilterInternal_ValidToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/cards");
        when(request.getHeader("Authorization")).thenReturn(JwtConstants.BEARER_PREFIX + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, userDetails)).thenReturn(true);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_NoAuthorizationHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/cards");
        when(request.getHeader("Authorization")).thenReturn(null);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_InvalidToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/cards");
        when(request.getHeader("Authorization")).thenReturn(JwtConstants.BEARER_PREFIX + "invalid-token");
        when(jwtUtil.extractUsername("invalid-token")).thenThrow(new RuntimeException("Invalid token"));
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_SwaggerPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }
    
    @Test
    void testDoFilterInternal_ApiDocsPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/v3/api-docs");
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }
    
    @Test
    void testDoFilterInternal_InvalidTokenValidation() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/cards");
        when(request.getHeader("Authorization")).thenReturn(JwtConstants.BEARER_PREFIX + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, userDetails)).thenReturn(false);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_NoBearerPrefix() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/cards");
        when(request.getHeader("Authorization")).thenReturn("Invalid " + validToken);
        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }
}
