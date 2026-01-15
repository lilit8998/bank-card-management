package bank.card.management.exception;

public class CardNotFoundException extends BusinessException {
    
    public CardNotFoundException(String message) {
        super(message);
    }
    
    public CardNotFoundException(Long cardId) {
        super("Card not found with id: " + cardId);
    }
}
