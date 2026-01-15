package bank.card.management.service;

import bank.card.management.dto.request.CreateCardRequest;
import bank.card.management.dto.response.AdminCardResponse;
import bank.card.management.dto.response.CardResponse;
import bank.card.management.entity.BankCard;
import bank.card.management.entity.CardStatus;
import bank.card.management.entity.User;
import bank.card.management.exception.CardAlreadyExistsException;
import bank.card.management.exception.CardExpiredException;
import bank.card.management.exception.CardNotFoundException;
import bank.card.management.mapper.AdminCardResponseMapper;
import bank.card.management.mapper.CardResponseMapper;
import bank.card.management.repository.BankCardRepository;
import bank.card.management.repository.UserRepository;
import bank.card.management.service.impl.CardServiceImpl;
import bank.card.management.util.CardNumberMasker;
import bank.card.management.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {
    
    @Mock
    private BankCardRepository cardRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EncryptionUtil encryptionUtil;
    
    @Mock
    private CardNumberMasker cardNumberMasker;
    
    @Mock
    private CardResponseMapper cardResponseMapper;
    
    @Mock
    private AdminCardResponseMapper adminCardResponseMapper;
    
    @InjectMocks
    private CardServiceImpl cardService;
    
    private User testUser;
    private BankCard testCard;
    private CreateCardRequest createCardRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testCard = new BankCard();
        testCard.setId(1L);
        testCard.setCardNumber("encrypted123");
        testCard.setCardNumberMasked("**** **** **** 1234");
        testCard.setOwner("John Doe");
        testCard.setExpiryDate(LocalDate.now().plusYears(2));
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setBalance(new BigDecimal("1000.00"));
        testCard.setUser(testUser);
        
        createCardRequest = new CreateCardRequest();
        createCardRequest.setCardNumber("1234567890123456");
        createCardRequest.setOwner("John Doe");
        createCardRequest.setExpiryDate(LocalDate.now().plusYears(2));
        createCardRequest.setInitialBalance(new BigDecimal("1000.00"));
    }
    
    @Test
    void testCreateCard_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardNumberMasker.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
        when(cardRepository.existsByCardNumberMasked("**** **** **** 3456")).thenReturn(false);
        when(encryptionUtil.encrypt("1234567890123456")).thenReturn("encrypted123");
        when(cardRepository.save(any(BankCard.class))).thenReturn(testCard);
        
        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(1L);
        cardResponse.setCardNumber("**** **** **** 1234");
        when(cardResponseMapper.toCardResponse(any(BankCard.class))).thenReturn(cardResponse);
        
        CardResponse result = cardService.createCard(createCardRequest, "testuser");
        
        assertNotNull(result);
        verify(cardRepository, times(1)).save(any(BankCard.class));
        verify(cardResponseMapper, times(1)).toCardResponse(any(BankCard.class));
    }
    
    @Test
    void testCreateCard_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, 
            () -> cardService.createCard(createCardRequest, "testuser"));
    }
    
    @Test
    void testCreateCard_CardAlreadyExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardNumberMasker.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
        when(cardRepository.existsByCardNumberMasked("**** **** **** 3456")).thenReturn(true);
        
        assertThrows(CardAlreadyExistsException.class, 
            () -> cardService.createCard(createCardRequest, "testuser"));
    }
    
    @Test
    void testCreateCard_ExpiredCard() {
        createCardRequest.setExpiryDate(LocalDate.now().minusDays(1));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardNumberMasker.maskCardNumber("1234567890123456")).thenReturn("**** **** **** 3456");
        when(cardRepository.existsByCardNumberMasked("**** **** **** 3456")).thenReturn(false);
        when(encryptionUtil.encrypt("1234567890123456")).thenReturn("encrypted123");
        
        BankCard expiredCard = new BankCard();
        expiredCard.setStatus(CardStatus.EXPIRED);
        when(cardRepository.save(any(BankCard.class))).thenReturn(expiredCard);
        
        CardResponse cardResponse = new CardResponse();
        cardResponse.setStatus(CardStatus.EXPIRED);
        when(cardResponseMapper.toCardResponse(any(BankCard.class))).thenReturn(cardResponse);
        
        CardResponse result = cardService.createCard(createCardRequest, "testuser");
        
        assertNotNull(result);
        assertEquals(CardStatus.EXPIRED, result.getStatus());
    }
    
    @Test
    void testGetUserCards_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BankCard> cardPage = new PageImpl<>(Arrays.asList(testCard));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByUser(testUser, pageable)).thenReturn(cardPage);
        
        CardResponse cardResponse = new CardResponse();
        when(cardResponseMapper.toCardResponse(testCard)).thenReturn(cardResponse);
        
        Page<CardResponse> result = cardService.getUserCards("testuser", pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
    
    @Test
    void testGetCardById_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testCard));
        
        CardResponse cardResponse = new CardResponse();
        when(cardResponseMapper.toCardResponse(testCard)).thenReturn(cardResponse);
        
        CardResponse result = cardService.getCardById(1L, "testuser");
        
        assertNotNull(result);
    }
    
    @Test
    void testGetCardById_CardNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());
        
        assertThrows(CardNotFoundException.class, 
            () -> cardService.getCardById(1L, "testuser"));
    }
    
    @Test
    void testGetCardByIdAdmin_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        
        AdminCardResponse adminResponse = new AdminCardResponse();
        adminResponse.setId(1L);
        adminResponse.setUserId(1L);
        when(adminCardResponseMapper.toAdminCardResponse(testCard)).thenReturn(adminResponse);
        
        AdminCardResponse result = cardService.getCardByIdAdmin(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
    }
    
    @Test
    void testGetCardByIdAdmin_CardNotFound() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(CardNotFoundException.class, 
            () -> cardService.getCardByIdAdmin(1L));
    }
    
    @Test
    void testBlockCard_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(testCard)).thenReturn(testCard);
        
        CardResponse cardResponse = new CardResponse();
        cardResponse.setStatus(CardStatus.BLOCKED);
        when(cardResponseMapper.toCardResponse(testCard)).thenReturn(cardResponse);
        
        CardResponse result = cardService.blockCard(1L, "testuser");
        
        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository, times(1)).save(testCard);
    }
    
    @Test
    void testBlockCard_ExpiredCard() {
        testCard.setStatus(CardStatus.EXPIRED);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testCard));
        
        assertThrows(CardExpiredException.class, 
            () -> cardService.blockCard(1L, "testuser"));
    }
    
    @Test
    void testActivateCard_Success() {
        testCard.setStatus(CardStatus.BLOCKED);
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(testCard)).thenReturn(testCard);
        
        AdminCardResponse adminResponse = new AdminCardResponse();
        adminResponse.setStatus(CardStatus.ACTIVE);
        when(adminCardResponseMapper.toAdminCardResponse(testCard)).thenReturn(adminResponse);
        
        AdminCardResponse result = cardService.activateCard(1L);
        
        assertNotNull(result);
        assertEquals(CardStatus.ACTIVE, testCard.getStatus());
        verify(cardRepository, times(1)).save(testCard);
    }
    
    @Test
    void testActivateCard_ExpiredCard() {
        testCard.setExpiryDate(LocalDate.now().minusDays(1));
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        
        assertThrows(CardExpiredException.class, 
            () -> cardService.activateCard(1L));
    }
    
    @Test
    void testBlockCardAdmin_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(testCard)).thenReturn(testCard);
        
        AdminCardResponse adminResponse = new AdminCardResponse();
        adminResponse.setStatus(CardStatus.BLOCKED);
        when(adminCardResponseMapper.toAdminCardResponse(testCard)).thenReturn(adminResponse);
        
        AdminCardResponse result = cardService.blockCardAdmin(1L);
        
        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository, times(1)).save(testCard);
    }
    
    @Test
    void testDeleteCard_Success() {
        when(cardRepository.existsById(1L)).thenReturn(true);
        
        cardService.deleteCard(1L);
        
        verify(cardRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void testDeleteCard_CardNotFound() {
        when(cardRepository.existsById(1L)).thenReturn(false);
        
        assertThrows(CardNotFoundException.class, 
            () -> cardService.deleteCard(1L));
    }
    
    @Test
    void testGetUserActiveCards_Success() {
        BankCard activeCard1 = new BankCard();
        activeCard1.setStatus(CardStatus.ACTIVE);
        BankCard activeCard2 = new BankCard();
        activeCard2.setStatus(CardStatus.ACTIVE);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByUserAndStatus(testUser, CardStatus.ACTIVE))
            .thenReturn(Arrays.asList(activeCard1, activeCard2));
        
        CardResponse response1 = new CardResponse();
        CardResponse response2 = new CardResponse();
        when(cardResponseMapper.toCardResponse(activeCard1)).thenReturn(response1);
        when(cardResponseMapper.toCardResponse(activeCard2)).thenReturn(response2);
        
        List<CardResponse> result = cardService.getUserActiveCards("testuser");
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
