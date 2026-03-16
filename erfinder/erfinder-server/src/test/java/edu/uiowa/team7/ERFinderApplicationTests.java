package edu.uiowa.team7;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ERFinderApplicationTests {

    @Test
    void contextLoads() {
        // Validates that the Spring Boot application starts without crashing
    }

    @Test
    void testDatabaseConnection() {
        // Validates that Queries.java successfully connects to MySQL
        assertNotNull(Queries.GetConnection(), "The database connection should be successfully established.");
    }
}