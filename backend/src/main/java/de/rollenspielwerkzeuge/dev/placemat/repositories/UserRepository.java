package de.rollenspielwerkzeuge.dev.placemat.repositories;

import de.rollenspielwerkzeuge.dev.placemat.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
}