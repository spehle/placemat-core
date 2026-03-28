package de.rollenspielwerkzeuge.dev.placemat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Boots the full Spring application context for integration testing. The test uses a real PostgreSQL
 * database started via Testcontainers so that Flyway migrations and Hibernate mappings are validated
 * against the same database engine used in development and production.
 */
@Testcontainers
@SpringBootTest
class PlacematCoreBackendApplicationTests {

	/**
	 * Provides a PostgreSQL database for the Spring Boot test context. The DataSource is automatically
	 * configured through Spring Boot's Testcontainers service connection support.
	 */
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	/**
	 * Verifies that the application context starts successfully with Flyway migrations applied.
	 * This is a smoke test for the overall configuration.
	 */
	@Test
	void contextLoads() {
		// Intentionally empty.
	}
}
