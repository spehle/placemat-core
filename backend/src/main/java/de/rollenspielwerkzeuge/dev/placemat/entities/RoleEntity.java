package de.rollenspielwerkzeuge.dev.placemat.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a security role that can be assigned to users. Roles are intended to be stable identifiers
 * used by authorization checks (e.g. ADMIN, USER) and should therefore be unique and immutable in spirit.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity extends AuditableEntity {

    /**
     * The technical primary key used by the database. This identifier is not used for authorization checks
     * and must not be exposed as a stable public identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique role name used by authorization checks. This value must be unique and should be treated
     * as a stable code, for example "ADMIN" or "USER".
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
