package de.rollenspielwerkzeuge.dev.placemat.auth;

import de.rollenspielwerkzeuge.dev.placemat.entities.UserEntity;
import de.rollenspielwerkzeuge.dev.placemat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AuthBean {

    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserEntity u = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            var authorities = u.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                    .toList();

            return User.withUsername(u.getUsername())
                    .password(u.getPasswordHash())
                    .disabled(!u.isEnabled())
                    .authorities(authorities)
                    .build();
        };
    }
}
