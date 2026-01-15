package bank.card.management.exception;

public class EmailAlreadyInUseException extends BusinessException {
    
    public EmailAlreadyInUseException(String email) {
        super("Email '" + email + "' is already in use");
    }
}
