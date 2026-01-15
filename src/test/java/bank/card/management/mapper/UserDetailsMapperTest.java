package bank.card.management.mapper;

import bank.card.management.entity.Role;
import bank.card.management.entity.User;
import bank.card.management.entity.UserStatus;
import bank.card.management.util.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsMapperTest {
    
    @InjectMocks
    private UserDetailsMapper userDetailsMapper;
    
    private User testUser;
    private Role userRole;
    private Role adminRole;
    
    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);
        
        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(Role.RoleName.ADMIN);
        
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
        testUser.setStatus(UserStatus.ACTIVE);
    }
    
    @Test
    void testToUserDetails_ActiveUser() {
        UserDetails userDetails = userDetailsMapper.toUserDetails(testUser);
        
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        assertTrue(authorities.contains(RoleConstants.ROLE_PREFIX + RoleConstants.USER));
    }
    
    @Test
    void testToUserDetails_BlockedUser() {
        testUser.setStatus(UserStatus.BLOCKED);
        
        UserDetails userDetails = userDetailsMapper.toUserDetails(testUser);
        
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }
    
    @Test
    void testToUserDetails_MultipleRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);
        testUser.setRoles(roles);
        
        UserDetails userDetails = userDetailsMapper.toUserDetails(testUser);
        
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        assertTrue(authorities.contains(RoleConstants.ROLE_PREFIX + RoleConstants.USER));
        assertTrue(authorities.contains(RoleConstants.ROLE_PREFIX + RoleConstants.ADMIN));
        assertEquals(2, authorities.size());
    }
    
    @Test
    void testToUserDetails_NoRoles() {
        testUser.setRoles(new HashSet<>());
        
        UserDetails userDetails = userDetailsMapper.toUserDetails(testUser);
        
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().isEmpty());
    }
    
    @Test
    void testToUserDetails_AdminRole() {
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        testUser.setRoles(roles);
        
        UserDetails userDetails = userDetailsMapper.toUserDetails(testUser);
        
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        assertTrue(authorities.contains(RoleConstants.ROLE_PREFIX + RoleConstants.ADMIN));
        assertEquals(1, authorities.size());
    }
}
