package bank.card.management.mapper;

import bank.card.management.dto.response.CardResponse;
import bank.card.management.entity.BankCard;
import bank.card.management.entity.CardStatus;
import bank.card.management.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CardResponseMapperTest {
    
    private CardResponseMapper cardResponseMapper;
    private BankCard bankCard;
    private User user;
    
    @BeforeEach
    void setUp() {
        cardResponseMapper = Mappers.getMapper(CardResponseMapper.class);
        
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        bankCard = new BankCard();
        bankCard.setId(1L);
        bankCard.setCardNumber("encrypted123");
        bankCard.setCardNumberMasked("**** **** **** 1234");
        bankCard.setOwner("John Doe");
        bankCard.setExpiryDate(LocalDate.now().plusYears(2));
        bankCard.setStatus(CardStatus.ACTIVE);
        bankCard.setBalance(new BigDecimal("1000.50"));
        bankCard.setUser(user);
    }
    
    @Test
    void testToCardResponse_Success() {
        CardResponse response = cardResponseMapper.toCardResponse(bankCard);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("**** **** **** 1234", response.getCardNumber());
        assertEquals("John Doe", response.getOwner());
        assertEquals(LocalDate.now().plusYears(2), response.getExpiryDate());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        assertEquals(new BigDecimal("1000.50"), response.getBalance());
    }
    
    @Test
    void testToCardResponse_BlockedStatus() {
        bankCard.setStatus(CardStatus.BLOCKED);
        
        CardResponse response = cardResponseMapper.toCardResponse(bankCard);
        
        assertEquals(CardStatus.BLOCKED, response.getStatus());
    }
    
    @Test
    void testToCardResponse_ExpiredStatus() {
        bankCard.setStatus(CardStatus.EXPIRED);
        bankCard.setExpiryDate(LocalDate.now().minusDays(1));
        
        CardResponse response = cardResponseMapper.toCardResponse(bankCard);
        
        assertEquals(CardStatus.EXPIRED, response.getStatus());
        assertTrue(response.getExpiryDate().isBefore(LocalDate.now()));
    }
    
    @Test
    void testToCardResponse_ZeroBalance() {
        bankCard.setBalance(new BigDecimal("0.00"));
        
        CardResponse response = cardResponseMapper.toCardResponse(bankCard);
        
        assertEquals(new BigDecimal("0.00"), response.getBalance());
    }
    
    @Test
    void testToCardResponse_MaskedCardNumber() {
        bankCard.setCardNumberMasked("**** **** **** 5678");
        
        CardResponse response = cardResponseMapper.toCardResponse(bankCard);
        
        assertEquals("**** **** **** 5678", response.getCardNumber());
    }
}
