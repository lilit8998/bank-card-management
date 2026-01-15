package bank.card.management.service.impl;

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
import bank.card.management.service.CardService;
import bank.card.management.util.CardNumberMasker;
import bank.card.management.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    
    private final BankCardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardNumberMasker cardNumberMasker;
    private final CardResponseMapper cardResponseMapper;
    private final AdminCardResponseMapper adminCardResponseMapper;
    
    @Override
    @Transactional
    public CardResponse createCard(CreateCardRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        String maskedNumber = cardNumberMasker.maskCardNumber(request.getCardNumber());
        if (cardRepository.existsByCardNumberMasked(maskedNumber)) {
            throw new CardAlreadyExistsException(maskedNumber);
        }
        
        String encryptedCardNumber = encryptionUtil.encrypt(request.getCardNumber());
        
        CardStatus status = request.getExpiryDate().isBefore(LocalDate.now())
                ? CardStatus.EXPIRED 
                : CardStatus.ACTIVE;
        
        BankCard card = new BankCard();
        card.setCardNumber(encryptedCardNumber);
        card.setCardNumberMasked(maskedNumber);
        card.setOwner(request.getOwner());
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(status);
        card.setBalance(request.getInitialBalance());
        card.setUser(user);
        
        BankCard savedCard = cardRepository.save(card);
        return cardResponseMapper.toCardResponse(savedCard);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return cardRepository.findByUser(user, pageable)
                .map(cardResponseMapper::toCardResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AdminCardResponse> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(adminCardResponseMapper::toAdminCardResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> searchUserCards(String username, String searchTerm, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return cardRepository.findByUserAndSearchTerm(user, searchTerm, pageable)
                .map(cardResponseMapper::toCardResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long cardId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        BankCard card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new CardNotFoundException("Card not found or access denied"));
        
        return cardResponseMapper.toCardResponse(card);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AdminCardResponse getCardByIdAdmin(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        
        return adminCardResponseMapper.toAdminCardResponse(card);
    }
    
    @Override
    @Transactional
    public CardResponse blockCard(Long cardId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        BankCard card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new CardNotFoundException("Card not found or access denied"));
        
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardExpiredException("Cannot block expired card");
        }
        
        card.setStatus(CardStatus.BLOCKED);
        BankCard savedCard = cardRepository.save(card);
        return cardResponseMapper.toCardResponse(savedCard);
    }
    
    @Override
    @Transactional
    public AdminCardResponse activateCard(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new CardExpiredException("Cannot activate expired card");
        }
        
        card.setStatus(CardStatus.ACTIVE);
        BankCard savedCard = cardRepository.save(card);
        return adminCardResponseMapper.toAdminCardResponse(savedCard);
    }
    
    @Override
    @Transactional
    public AdminCardResponse blockCardAdmin(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardExpiredException("Cannot block expired card");
        }
        
        card.setStatus(CardStatus.BLOCKED);
        BankCard savedCard = cardRepository.save(card);
        return adminCardResponseMapper.toAdminCardResponse(savedCard);
    }
    
    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException(cardId);
        }
        cardRepository.deleteById(cardId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getUserActiveCards(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return cardRepository.findByUserAndStatus(user, CardStatus.ACTIVE)
                .stream()
                .map(cardResponseMapper::toCardResponse)
                .collect(Collectors.toList());
    }
}
