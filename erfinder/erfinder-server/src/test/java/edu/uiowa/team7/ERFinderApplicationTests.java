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

    // tries to get a test user
    @Test
    void getTestEntry() {
        try (Connection c = Queries.GetConnection()) {
            assert(Queries.ValidateCredentials("user3","password3"));
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
            Queries.CreateTestUser(
                    "user3",
                    "password3",
                    "test_question",
                    "test_answer");

            assert(Queries.ValidateCredentials("user3", "password3"));

            // user3 already exists -- should throw error!!
            Queries.CreateTestUser(
                    "user3",
                    "password3",
                    "test_question_irrelevant",
                    "test_answer_irrelevant");

            assert(false);
        } catch (SQLException e) {

        }
    }

    @Test
    void JWTTokenParseUsername() {
        String token = Security.GenerateJWT("user3","test");

        Security.TokenParseResult parsedUsername = Security.ParseRequestJWT(token, "test");

        assert(parsedUsername.UserID().equals("user3"));
    }

    @Test
    void JWTTokenDoNotAcceptDifferentDevice() {
        String token = Security.GenerateJWT("user3", "test1");

        Security.TokenParseResult parsedUsername = Security.ParseRequestJWT(token, "test2");

        assert(!parsedUsername.IsValid());
    }

    @Test
    void JWTTokenShouldRefresh() {

    }
}
