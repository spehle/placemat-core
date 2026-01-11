package de.rollenspielwerkzeuge.dev.placemat.seeder;

import de.rollenspielwerkzeuge.dev.placemat.entities.RoleEntity;
import de.rollenspielwerkzeuge.dev.placemat.entities.UserEntity;
import de.rollenspielwerkzeuge.dev.placemat.repositories.RoleRepository;
import de.rollenspielwerkzeuge.dev.placemat.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.*;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) {
        var adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new RoleEntity(null, "ADMIN")));

        userRepository.findByUsername("admin").orElseGet(() -> {
            var u = new UserEntity();
            u.setUsername("admin");
            u.setPasswordHash(encoder.encode("admin"));
            u.getRoles().add(adminRole);
            return userRepository.save(u);
        });
    }
}
