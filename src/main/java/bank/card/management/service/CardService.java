package bank.card.management.service;

import bank.card.management.dto.request.CreateCardRequest;
import bank.card.management.dto.response.AdminCardResponse;
import bank.card.management.dto.response.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {
    CardResponse createCard(CreateCardRequest request, String username);

    Page<CardResponse> getUserCards(String username, Pageable pageable);

    Page<AdminCardResponse> getAllCards(Pageable pageable);

    Page<CardResponse> searchUserCards(String username, String searchTerm, Pageable pageable);

    CardResponse getCardById(Long cardId, String username);

    AdminCardResponse getCardByIdAdmin(Long cardId);

    CardResponse blockCard(Long cardId, String username);

    AdminCardResponse activateCard(Long cardId);

    AdminCardResponse blockCardAdmin(Long cardId);

    void deleteCard(Long cardId);

    List<CardResponse> getUserActiveCards(String username);
}
