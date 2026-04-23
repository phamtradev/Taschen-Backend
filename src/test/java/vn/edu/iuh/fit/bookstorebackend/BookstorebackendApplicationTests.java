package vn.edu.iuh.fit.bookstorebackend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test - requires running database.
 * Run manually with Docker: docker-compose up -d
 * or exclude when running CI without database.
 */
@SpringBootTest
@Disabled("Requires running database. Run with: docker-compose up -d")
class BookstorebackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
