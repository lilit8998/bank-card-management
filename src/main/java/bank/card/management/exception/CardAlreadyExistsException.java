package bank.card.management.exception;

public class CardAlreadyExistsException extends BusinessException {
    
    public CardAlreadyExistsException(String maskedNumber) {
        super("Card with number " + maskedNumber + " already exists");
    }
}
