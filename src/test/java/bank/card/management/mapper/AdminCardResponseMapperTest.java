package bank.card.management.mapper;

import bank.card.management.dto.response.AdminCardResponse;
import bank.card.management.entity.BankCard;
import bank.card.management.entity.CardStatus;
import bank.card.management.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AdminCardResponseMapperTest {
    
    private AdminCardResponseMapper adminCardResponseMapper;
    private BankCard bankCard;
    private User user;
    
    @BeforeEach
    void setUp() {
        adminCardResponseMapper = Mappers.getMapper(AdminCardResponseMapper.class);
        
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
    void testToAdminCardResponse_Success() {
        AdminCardResponse response = adminCardResponseMapper.toAdminCardResponse(bankCard);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("**** **** **** 1234", response.getCardNumber());
        assertEquals("John Doe", response.getOwner());
        assertEquals(LocalDate.now().plusYears(2), response.getExpiryDate());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        assertEquals(new BigDecimal("1000.50"), response.getBalance());
        assertEquals(1L, response.getUserId());
    }
    
    @Test
    void testToAdminCardResponse_UserIdIncluded() {
        user.setId(99L);
        bankCard.setUser(user);
        
        AdminCardResponse response = adminCardResponseMapper.toAdminCardResponse(bankCard);
        
        assertEquals(99L, response.getUserId());
    }
    
    @Test
    void testToAdminCardResponse_ExpiredCard() {
        bankCard.setStatus(CardStatus.EXPIRED);
        bankCard.setExpiryDate(LocalDate.now().minusDays(1));
        
        AdminCardResponse response = adminCardResponseMapper.toAdminCardResponse(bankCard);
        
        assertEquals(CardStatus.EXPIRED, response.getStatus());
        assertTrue(response.getExpiryDate().isBefore(LocalDate.now()));
    }
    
    @Test
    void testToAdminCardResponse_BlockedCard() {
        bankCard.setStatus(CardStatus.BLOCKED);
        
        AdminCardResponse response = adminCardResponseMapper.toAdminCardResponse(bankCard);
        
        assertEquals(CardStatus.BLOCKED, response.getStatus());
        assertEquals(1L, response.getUserId());
    }
    
    @Test
    void testToAdminCardResponse_DifferentUser() {
        User differentUser = new User();
        differentUser.setId(42L);
        bankCard.setUser(differentUser);
        
        AdminCardResponse response = adminCardResponseMapper.toAdminCardResponse(bankCard);
        
        assertEquals(42L, response.getUserId());
    }
}
