package de.rollenspielwerkzeuge.dev.placemat.config;

import de.rollenspielwerkzeuge.dev.placemat.entities.UserEntity;
import de.rollenspielwerkzeuge.dev.placemat.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves the current application user for JPA auditing.
 *
 * <p>This implementation integrates Spring Data JPA auditing with Spring Security.
 * It determines the current auditor based on the active {@link Authentication}
 * stored in the {@link SecurityContextHolder}.</p>
 *
 * <p>If no authenticated user is available (e.g. during application startup,
 * database seeding, or unauthenticated system operations), an empty {@code Optional}
 * is returned. In such cases, {@code createdBy} and {@code updatedBy} may remain
 * {@code null}, depending on database constraints.</p>
 */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<UserEntity> {

    private final UserRepository userRepository;
    private final boolean fallbackEnabled;
    private final String fallbackUsername;


    public SpringSecurityAuditorAware(
            UserRepository userRepository,
            @Value("${placemat.auditing.fallback-enabled:false}") boolean fallbackEnabled,
            @Value("${placemat.auditing.fallback-username:admin}") String fallbackUsername) {
        this.userRepository = userRepository;
        this.fallbackEnabled = fallbackEnabled;
        this.fallbackUsername = fallbackUsername;
    }


    /**
     * Returns the current auditor (user) if available.
     *
     * <p>The username is extracted from the Spring Security authentication and
     * resolved to a {@link UserEntity} via the repository. Only authenticated
     * principals are considered.</p>
     *
     * @return an {@code Optional} containing the current user, or empty if no
     *         authenticated user is present
     */
    @Override
    public Optional<UserEntity> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            if (!fallbackEnabled) {
                return Optional.empty();
            }
            return userRepository.findByUsername(fallbackUsername);
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }
}
