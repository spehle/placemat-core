package de.rollenspielwerkzeuge.dev.placemat.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class for entities that require auditing information.
 *
 * <p>This class captures both timestamps and actors for creation and the latest update.
 * The values are maintained automatically by Spring Data JPA Auditing and must not be
 * set by business logic. By inheriting from this class, domain entities gain a
 * consistent and centralized audit trail.</p>
 *
 * <p>Note that {@code createdBy} and {@code updatedBy} may be {@code null} in early
 * development stages (e.g. seeders, unauthenticated bootstrap runs), depending on
 * how {@code AuditorAware} is configured.</p>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    /** Timestamp of initial persistence (insert). Managed by Spring Data auditing. */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdOn;

    /** Timestamp of the most recent update. Managed by Spring Data auditing. */
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedOn;

    /**
     * User who initially created the entity.
     *
     * <p>This reference is resolved via {@code AuditorAware} based on the currently
     * authenticated principal (e.g. JWT subject/username). It is stored as a relation
     * to the user entity for traceability.</p>
     */
    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    /**
     * User who last modified the entity.
     *
     * <p>This reference is resolved via {@code AuditorAware} on each update. The value
     * represents the actor of the latest change and is useful for quick inspection
     * without consulting a separate change log.</p>
     */
    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserEntity updatedBy;

    /**
     * Returns the timestamp when the entity was created.
     *
     * <p>The value is set once during insert and never updated afterwards. Use this
     * for auditing, sorting, or time-based retention logic.</p>
     */
    public Instant getCreatedOn() {
        return createdOn;
    }

    /**
     * Returns the timestamp of the most recent update to the entity.
     *
     * <p>The value is updated automatically on each persistable change. This is a
     * lightweight "last touched" indicator and can be complemented by an explicit
     * change log if deeper history is required.</p>
     */
    public Instant getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Returns the user who created the entity.
     *
     * <p>Depending on the runtime context, this may be {@code null} (e.g. initial
     * data seeding). In authenticated request flows it should be set consistently.</p>
     */
    public UserEntity getCreatedBy() {
        return createdBy;
    }

    /**
     * Returns the user who last modified the entity.
     *
     * <p>This value reflects the actor of the latest change. It is useful for quick
     * diagnostics and can be used together with {@code updatedOn} to answer "who
     * changed this last, and when?".</p>
     */
    public UserEntity getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets the creation timestamp.
     *
     * <p>This setter is intended for JPA/auditing infrastructure only. Business code
     * should never modify audit fields directly.</p>
     */
    protected void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Sets the last-modified timestamp.
     *
     * <p>This setter is intended for JPA/auditing infrastructure only. Business code
     * should never modify audit fields directly.</p>
     */
    protected void setUpdatedOn(Instant updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     * Sets the creating user.
     *
     * <p>This setter is intended for auditing infrastructure only. The current auditor
     * is typically resolved via Spring Security and {@code AuditorAware}.</p>
     */
    protected void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Sets the last-modifying user.
     *
     * <p>This setter is intended for auditing infrastructure only. The current auditor
     * is typically resolved via Spring Security and {@code AuditorAware}.</p>
     */
    protected void setUpdatedBy(UserEntity updatedBy) {
        this.updatedBy = updatedBy;
    }
}
