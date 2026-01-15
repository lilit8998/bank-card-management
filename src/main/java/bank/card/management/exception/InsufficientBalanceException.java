package bank.card.management.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BusinessException {
    
    public InsufficientBalanceException(BigDecimal available, BigDecimal required) {
        super(String.format("Insufficient balance. Available: %s, Required: %s", available, required));
    }
}
