package bank.card.management.exception;

public class UsernameAlreadyTakenException extends BusinessException {
    
    public UsernameAlreadyTakenException(String username) {
        super("Username '" + username + "' is already taken");
    }
}
