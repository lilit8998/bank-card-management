package bank.card.management.util;

import org.springframework.stereotype.Component;

@Component
public class CardNumberMasker {
    
    private static final String MASK_CHAR = "*";
    private static final int VISIBLE_DIGITS = 4;

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < VISIBLE_DIGITS) {
            return cardNumber;
        }
        
        // Удаляем все пробелы и нецифровые символы
        String cleaned = cardNumber.replaceAll("[^0-9]", "");
        
        if (cleaned.length() < VISIBLE_DIGITS) {
            return cardNumber;
        }
        
        String lastFour = cleaned.substring(cleaned.length() - VISIBLE_DIGITS);
        
        int totalDigits = cleaned.length();
        int maskedDigits = totalDigits - VISIBLE_DIGITS;
        
        StringBuilder masked = new StringBuilder();
        int position = 0;
        
        while (position < maskedDigits) {
            if (masked.length() > 0) {
                masked.append(" ");
            }
            int groupSize = Math.min(4, maskedDigits - position);
            masked.append(MASK_CHAR.repeat(groupSize));
            position += groupSize;
        }
        
        if (masked.length() > 0) {
            masked.append(" ");
        }
        masked.append(lastFour);
        
        return masked.toString();
    }

    public String getLastFourDigits(String cardNumber) {
        if (cardNumber == null) {
            return "";
        }
        String cleaned = cardNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() < VISIBLE_DIGITS) {
            return cleaned;
        }
        return cleaned.substring(cleaned.length() - VISIBLE_DIGITS);
    }
}

