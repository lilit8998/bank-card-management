package bank.card.management.repository;

import bank.card.management.entity.BankCard;
import bank.card.management.entity.CardStatus;
import bank.card.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {
    
    Page<BankCard> findByUser(User user, Pageable pageable);
    
    Page<BankCard> findByUserAndStatus(User user, CardStatus status, Pageable pageable);
    
    @Query("SELECT c FROM BankCard c WHERE c.user = :user AND " +
           "(LOWER(c.owner) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "c.cardNumberMasked LIKE CONCAT('%', :search, '%'))")
    Page<BankCard> findByUserAndSearchTerm(@Param("user") User user, 
                                           @Param("search") String search, 
                                           Pageable pageable);
    
    List<BankCard> findByUserAndStatus(User user, CardStatus status);
    
    Optional<BankCard> findByIdAndUser(Long id, User user);
    
    boolean existsByCardNumberMasked(String cardNumberMasked);
}

