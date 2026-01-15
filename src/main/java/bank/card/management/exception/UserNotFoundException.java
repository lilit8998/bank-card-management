package bank.card.management.exception;

public class UserNotFoundException extends BusinessException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId);
    }
}
