package bank.card.management.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardNumberMaskerTest {
    
    private CardNumberMasker cardNumberMasker;
    
    @BeforeEach
    void setUp() {
        cardNumberMasker = new CardNumberMasker();
    }
    
    @Test
    void testMaskCardNumber_Standard16Digits() {
        String cardNumber = "1234567890123456";
        String masked = cardNumberMasker.maskCardNumber(cardNumber);
        assertEquals("**** **** **** 3456", masked);
    }
    
    @Test
    void testMaskCardNumber_WithSpaces() {
        String cardNumber = "1234 5678 9012 3456";
        String masked = cardNumberMasker.maskCardNumber(cardNumber);
        assertEquals("**** **** **** 3456", masked);
    }
    
    @Test
    void testMaskCardNumber_ShortNumber() {
        String cardNumber = "1234";
        String masked = cardNumberMasker.maskCardNumber(cardNumber);
        assertEquals("1234", masked);
    }
    
    @Test
    void testGetLastFourDigits() {
        String cardNumber = "1234567890123456";
        String lastFour = cardNumberMasker.getLastFourDigits(cardNumber);
        assertEquals("3456", lastFour);
    }
}

