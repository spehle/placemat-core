package de.rollenspielwerkzeuge.dev.placemat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing for the application.
 *
 * <p>This configuration activates @CreatedDate, @LastModifiedDate, @CreatedBy, and
 * @LastModifiedBy handling via an {@code AuditorAware} implementation. The auditor
 * is resolved from the current Spring Security context.</p>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
}