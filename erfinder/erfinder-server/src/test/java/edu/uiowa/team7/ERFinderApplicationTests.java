package edu.uiowa.team7;

import java.sql.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ERFinderApplicationTests {

    @Test
    void contextLoads() {
        // If the database connection in application.properties is wrong, this test will fail.
    }

    @Test
    void establishDBConnection() {
        Queries.GetConnection();
    }

    // tries to get a user w/ id = 1 in the 'users' table.
    @Test
    void getTestEntry() {
        try (Connection c = Queries.GetConnection()) {
            assert(Queries.ValidateCredentials("user1","password"));
        } catch (SQLException ex) {
            Queries.PrintSQLException(ex);
            assert (false);
        } catch (Exception e) {
            assert(false);
        }
    }

    @Test
    void CreateUser() {
        try {
            // clear out test users
            Queries.PurgeTestUsers();

            // add user 3
            Queries.CreateTestUser("user3", "password3");

            assert(Queries.ValidateCredentials("user3", "password3"));

            Queries.CreateTestUser("user3", "password3");

            assert(false);
        } catch (SQLException e) {

        }
    }
}
