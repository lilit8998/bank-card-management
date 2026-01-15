package bank.card.management.service;

import bank.card.management.dto.response.UserResponse;
import bank.card.management.entity.Role;
import bank.card.management.entity.User;
import bank.card.management.entity.UserStatus;
import bank.card.management.exception.UserNotFoundException;
import bank.card.management.mapper.UserMapper;
import bank.card.management.repository.UserRepository;
import bank.card.management.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private User testUser;
    private UserResponse userResponse;
    private Role userRole;
    
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
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRoles(roles);
        testUser.setStatus(UserStatus.ACTIVE);
        
        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        userResponse.setStatus(UserStatus.ACTIVE);
    }
    
    @Test
    void testGetAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserResponse(testUser)).thenReturn(userResponse);
        
        Page<UserResponse> result = userService.getAllUsers(pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("testuser", result.getContent().get(0).getUsername());
        verify(userMapper, times(1)).toUserResponse(testUser);
    }
    
    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toUserResponse(testUser)).thenReturn(userResponse);
        
        UserResponse result = userService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userMapper, times(1)).toUserResponse(testUser);
    }
    
    @Test
    void testGetUserById_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(UserNotFoundException.class, 
            () -> userService.getUserById(1L));
        
        verify(userMapper, never()).toUserResponse(any(User.class));
    }
    
    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        
        userService.deleteUser(1L);
        
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void testDeleteUser_UserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        
        assertThrows(UserNotFoundException.class, 
            () -> userService.deleteUser(1L));
        
        verify(userRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testBlockUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        
        UserResponse blockedResponse = new UserResponse();
        blockedResponse.setStatus(UserStatus.BLOCKED);
        when(userMapper.toUserResponse(testUser)).thenReturn(blockedResponse);
        
        UserResponse result = userService.blockUser(1L);
        
        assertNotNull(result);
        assertEquals(UserStatus.BLOCKED, testUser.getStatus());
        verify(userRepository, times(1)).save(testUser);
    }
    
    @Test
    void testUnblockUser_Success() {
        testUser.setStatus(UserStatus.BLOCKED);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        
        UserResponse activeResponse = new UserResponse();
        activeResponse.setStatus(UserStatus.ACTIVE);
        when(userMapper.toUserResponse(testUser)).thenReturn(activeResponse);
        
        UserResponse result = userService.unblockUser(1L);
        
        assertNotNull(result);
        assertEquals(UserStatus.ACTIVE, testUser.getStatus());
        verify(userRepository, times(1)).save(testUser);
    }
    
    @Test
    void testBlockUser_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(UserNotFoundException.class, 
            () -> userService.blockUser(1L));
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testUnblockUser_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(UserNotFoundException.class, 
            () -> userService.unblockUser(1L));
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testGetAllUsers_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(Arrays.asList());
        
        when(userRepository.findAll(pageable)).thenReturn(emptyPage);
        
        Page<UserResponse> result = userService.getAllUsers(pageable);
        
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
    
    @Test
    void testGetAllUsers_MultipleUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        
        UserResponse response2 = new UserResponse();
        response2.setId(2L);
        response2.setUsername("user2");
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser, user2));
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserResponse(testUser)).thenReturn(userResponse);
        when(userMapper.toUserResponse(user2)).thenReturn(response2);
        
        Page<UserResponse> result = userService.getAllUsers(pageable);
        
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(userMapper, times(2)).toUserResponse(any(User.class));
    }
}
