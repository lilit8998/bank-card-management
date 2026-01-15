package bank.card.management.exception;

public class RoleNotFoundException extends BusinessException {
    
    public RoleNotFoundException(String roleName) {
        super("Role '" + roleName + "' is not found");
    }
}
