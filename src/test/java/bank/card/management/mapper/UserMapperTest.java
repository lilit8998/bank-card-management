package bank.card.management.mapper;

import bank.card.management.dto.response.UserResponse;
import bank.card.management.entity.Role;
import bank.card.management.entity.User;
import bank.card.management.util.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    
    private UserMapper userMapper;
    private User user;
    private Role userRole;
    private Role adminRole;
    
    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(Role.RoleName.USER);
        
        adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName(Role.RoleName.ADMIN);
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRoles(roles);
    }
    
    @Test
    void testToUserResponse_Success() {
        UserResponse response = userMapper.toUserResponse(user);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertNotNull(response.getRoles());
        assertEquals(1, response.getRoles().size());
        assertTrue(response.getRoles().contains(RoleConstants.USER));
    }
    
    @Test
    void testToUserResponse_MultipleRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);
        user.setRoles(roles);
        
        UserResponse response = userMapper.toUserResponse(user);
        
        assertNotNull(response.getRoles());
        assertEquals(2, response.getRoles().size());
        assertTrue(response.getRoles().contains(RoleConstants.USER));
        assertTrue(response.getRoles().contains(RoleConstants.ADMIN));
    }
    
    @Test
    void testToUserResponse_NoRoles() {
        user.setRoles(new HashSet<>());
        
        UserResponse response = userMapper.toUserResponse(user);
        
        assertNotNull(response);
        assertNotNull(response.getRoles());
        assertTrue(response.getRoles().isEmpty());
    }
    
    @Test
    void testToUserResponse_AllFields() {
        UserResponse response = userMapper.toUserResponse(user);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }
}
