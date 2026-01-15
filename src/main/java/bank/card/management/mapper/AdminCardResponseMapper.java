package bank.card.management.mapper;

import bank.card.management.dto.response.AdminCardResponse;
import bank.card.management.entity.BankCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminCardResponseMapper {
    
    @Mapping(source = "cardNumberMasked", target = "cardNumber")
    @Mapping(source = "user.id", target = "userId")
    AdminCardResponse toAdminCardResponse(BankCard card);
}
