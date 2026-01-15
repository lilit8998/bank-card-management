package bank.card.management.entity;

public enum CardStatus {
    ACTIVE("Активна"),
    BLOCKED("Заблокирована"),
    EXPIRED("Истек срок");
    
    private final String description;
    
    CardStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

