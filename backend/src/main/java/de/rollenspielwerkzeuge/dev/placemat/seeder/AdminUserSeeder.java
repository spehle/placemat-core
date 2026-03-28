package de.rollenspielwerkzeuge.dev.placemat.seeder;

import de.rollenspielwerkzeuge.dev.placemat.entities.UserEntity;
import de.rollenspielwerkzeuge.dev.placemat.repositories.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Seeds the default admin user that is required for development and for bootstrapping
 * further seed data. This runner is idempotent and will not create a duplicate user
 * if the admin already exists.
 */
@Component
@Profile("dev")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminUserSeeder implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new seeder instance. The dependencies are injected by Spring and are
     * required to find or create the admin user in a consistent way.
     *
     * @param userRepository Repository used to check for an existing admin user and to persist a new one.
     * @param passwordEncoder Encoder used to store the admin password in encoded form.
     */
    public AdminUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Ensures that the default admin user exists. If it is missing, it will be created
     * with a deterministic username and password for local development.
     *
     * @param args Application arguments provided by Spring Boot.
     */
    @Override
    public void run(ApplicationArguments args) {
        Optional<UserEntity> existing = userRepository.findByUsername(ADMIN_USERNAME);
        if (existing.isPresent()) {
            return;
        }

        UserEntity admin = new UserEntity();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));

        userRepository.save(admin);
    }
}
