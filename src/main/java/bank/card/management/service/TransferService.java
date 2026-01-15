package bank.card.management.service;

import bank.card.management.dto.request.TransferRequest;
import bank.card.management.dto.response.TransferResponse;

public interface TransferService {
    TransferResponse transferBetweenOwnCards(TransferRequest request, String username);
}
