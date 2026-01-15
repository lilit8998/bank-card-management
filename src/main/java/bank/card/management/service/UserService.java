package bank.card.management.service;

import bank.card.management.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(Long userId);

    void deleteUser(Long userId);
    
    UserResponse blockUser(Long userId);
    
    UserResponse unblockUser(Long userId);
}
