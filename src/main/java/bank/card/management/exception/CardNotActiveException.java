package bank.card.management.exception;

public class CardNotActiveException extends BusinessException {
    
    public CardNotActiveException(String message) {
        super(message);
    }
}
