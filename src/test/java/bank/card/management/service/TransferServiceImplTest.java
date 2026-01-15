package bank.card.management.service;

import bank.card.management.dto.request.TransferRequest;
import bank.card.management.dto.response.TransferResponse;
import bank.card.management.entity.BankCard;
import bank.card.management.entity.CardStatus;
import bank.card.management.entity.User;
import bank.card.management.exception.CardNotActiveException;
import bank.card.management.exception.CardNotFoundException;
import bank.card.management.exception.InsufficientBalanceException;
import bank.card.management.exception.TransferException;
import bank.card.management.repository.BankCardRepository;
import bank.card.management.repository.UserRepository;
import bank.card.management.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {
    
    @Mock
    private BankCardRepository cardRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private TransferServiceImpl transferService;
    
    private User testUser;
    private BankCard fromCard;
    private BankCard toCard;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        fromCard = new BankCard();
        fromCard.setId(1L);
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setUser(testUser);
        
        toCard = new BankCard();
        toCard.setId(2L);
        toCard.setBalance(new BigDecimal("500.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setUser(testUser);
    }
    
    @Test
    void testTransferBetweenOwnCards_Success() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200.00"));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(BankCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        TransferResponse response = transferService.transferBetweenOwnCards(request, "testuser");
        
        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertEquals("Transfer completed successfully", response.getMessage());
        assertEquals(new BigDecimal("800.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("700.00"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(BankCard.class));
    }
    
    @Test
    void testTransferBetweenOwnCards_UserNotFound() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200.00"));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, 
            () -> transferService.transferBetweenOwnCards(request, "testuser"));
        
        verify(cardRepository, never()).save(any(BankCard.class));
    }
    
    @Test
    void testTransferBetweenOwnCards_FromCardNotFound() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200.00"));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());
        
        assertThrows(CardNotFoundException.class, 
            () -> transferService.transferBetweenOwnCards(request, "testuser"));
        
        verify(cardRepository, never()).save(any(BankCard.class));
    }
    
    @Test
    void testTransferBetweenOwnCards_ToCardNotFound() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200.00"));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.empty());
        
        assertThrows(CardNotFoundException.class, 
            () -> transferService.transferBetweenOwnCards(request, "testuser"));
        
        verify(cardRepository, never()).save(any(BankCard.class));
    }
    
    @Test
    void testTransferBetweenOwnCards_SameCard() {
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("200.00"));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        
        assertThrows(TransferException.class, 
            () -> transferService.transferBetweenOwnCards(request, "testuser"));
        
        verify(cardRepository, never()).save(any(BankCard.class));
    }
    
    @Test
    void testTransferBetweenOwnCards_FromCardNotActive() {
        fromCard.setStatus(CardStatus.BLOCKED);
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200.00"));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.of(toCard));
        
        assertThrows(CardNotActiveException.class, 
            () -> transferService.transferBetweenOwnCards(request, "testuser"));
        
        verify(cardRepository, never()).save(any(BankCard.class));
    }
    
    @Test
    void testTransferBetweenOwnCards_ToCardNotActive() {
        toCard.setStatus(CardStatus.BLOCKED);
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("200.00"));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.of(toCard));
        
        assertThrows(CardNotActiveException.class, 
            () -> transferService.transferBetweenOwnCards(request, "testuser"));
        
        verify(cardRepository, never()).save(any(BankCard.class));
    }
    
    @Test
    void testTransferBetweenOwnCards_InsufficientBalance() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("2000.00"));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.of(toCard));
        
        assertThrows(InsufficientBalanceException.class, 
            () -> transferService.transferBetweenOwnCards(request, "testuser"));
        
        verify(cardRepository, never()).save(any(BankCard.class));
    }
    
    @Test
    void testTransferBetweenOwnCards_ExactBalance() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("1000.00"));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(BankCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        TransferResponse response = transferService.transferBetweenOwnCards(request, "testuser");
        
        assertNotNull(response);
        assertEquals(new BigDecimal("0.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("1500.00"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(BankCard.class));
    }
}
