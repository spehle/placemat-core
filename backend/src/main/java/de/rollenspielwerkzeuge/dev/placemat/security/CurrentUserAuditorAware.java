package de.rollenspielwerkzeuge.dev.placemat.security;

import de.rollenspielwerkzeuge.dev.placemat.entities.UserEntity;
import de.rollenspielwerkzeuge.dev.placemat.repositories.UserRepository;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Resolves the current auditor for Spring Data JPA auditing.
 *
 * <p>This implementation reads the current {@link Authentication} from the Spring Security
 * {@link org.springframework.security.core.context.SecurityContext}. If the request is
 * authenticated, it loads the corresponding {@link UserEntity} and returns it.</p>
 *
 * <p>If no authenticated principal is available (e.g. during bootstrap, seeders, or
 * background jobs), this returns {@link Optional#empty()}. This avoids accidental
 * fallback to a privileged user.</p>
 */
@Component("auditorAware")
public class CurrentUserAuditorAware implements AuditorAware<UserEntity> {

    private final UserRepository userRepository;

    /**
     * Creates a new auditor resolver.
     *
     * <p>The repository is used to translate the authenticated username into a persistent
     * {@link UserEntity} reference.</p>
     */
    public CurrentUserAuditorAware(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the current auditor entity if an authenticated user exists.
     *
     * <p>This method never falls back to a default user. If the security context is
     * missing or anonymous, it returns {@link Optional#empty()}.</p>
     */
    @Override
    public @NonNull Optional<UserEntity> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        String username = authentication.getName();
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        // Assumption: username is unique and is the authentication name.
        return userRepository.findByUsername(username);
    }
}
