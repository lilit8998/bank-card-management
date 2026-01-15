package bank.card.management.dto.response;

import bank.card.management.util.JwtConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = JwtConstants.BEARER_TYPE;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
}

