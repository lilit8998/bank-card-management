package bank.card.management.dto.response;

import bank.card.management.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCardResponse {
    private Long id;
    private String cardNumber; // Маскированный номер
    private String owner;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private Long userId; // ID владельца карты (только для админа)
}
