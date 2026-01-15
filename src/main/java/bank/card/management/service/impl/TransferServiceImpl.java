package bank.card.management.service.impl;

import bank.card.management.dto.request.TransferRequest;
import bank.card.management.dto.response.TransferResponse;
import bank.card.management.entity.BankCard;
import bank.card.management.entity.CardStatus;
import bank.card.management.entity.User;
import bank.card.management.exception.CardNotFoundException;
import bank.card.management.exception.CardNotActiveException;
import bank.card.management.exception.InsufficientBalanceException;
import bank.card.management.exception.TransferException;
import bank.card.management.repository.BankCardRepository;
import bank.card.management.repository.UserRepository;
import bank.card.management.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    
    private final BankCardRepository cardRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public TransferResponse transferBetweenOwnCards(TransferRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        BankCard fromCard = cardRepository.findByIdAndUser(request.getFromCardId(), user)
                .orElseThrow(() -> new CardNotFoundException("From card not found or access denied"));
        
        BankCard toCard = cardRepository.findByIdAndUser(request.getToCardId(), user)
                .orElseThrow(() -> new CardNotFoundException("To card not found or access denied"));
        
        if (fromCard.getId().equals(toCard.getId())) {
            throw new TransferException("Cannot transfer to the same card");
        }
        
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("From card is not active");
        }
        
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotActiveException("To card is not active");
        }
        
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(fromCard.getBalance(), request.getAmount());
        }
        
        UUID transactionId = UUID.randomUUID();
        
        BigDecimal newFromBalance = fromCard.getBalance().subtract(request.getAmount());
        BigDecimal newToBalance = toCard.getBalance().add(request.getAmount());
        
        fromCard.setBalance(newFromBalance);
        toCard.setBalance(newToBalance);
        
        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        
        return new TransferResponse(transactionId, "Transfer completed successfully");
    }
}
