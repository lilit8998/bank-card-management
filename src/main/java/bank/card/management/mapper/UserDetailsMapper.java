package bank.card.management.mapper;

import bank.card.management.entity.User;
import bank.card.management.entity.UserStatus;
import bank.card.management.util.RoleConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.security.core.userdetails.User.builder;

@Component
public class UserDetailsMapper {

    public UserDetails toUserDetails(User user) {
        boolean enabled = user.getStatus() == UserStatus.ACTIVE;
        
        return builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .disabled(!enabled)
                .build();
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(RoleConstants.ROLE_PREFIX + role.getName().name()))
                .collect(Collectors.toList());
    }
}
