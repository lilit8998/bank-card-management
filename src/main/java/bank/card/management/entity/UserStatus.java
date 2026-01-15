package bank.card.management.entity;

public enum UserStatus {
    ACTIVE("Активен"),
    BLOCKED("Заблокирован");
    
    private final String description;
    
    UserStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
