package bank.card.management.mapper;

import bank.card.management.dto.response.CardResponse;
import bank.card.management.entity.BankCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardResponseMapper {
    
    @Mapping(source = "cardNumberMasked", target = "cardNumber")
    CardResponse toCardResponse(BankCard card);
}
