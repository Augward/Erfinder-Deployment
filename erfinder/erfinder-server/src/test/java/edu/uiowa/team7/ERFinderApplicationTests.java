package edu.uiowa.team7;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import jakarta.servlet.http.Cookie;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "erfinder.login.max-attempts-in-period=10",
        "erfinder.login.clear-attempt-after-minutes=30",
        "erfinder.login.timeout-period-minutes=300",
        "spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:3306/erfinder",
        "spring.datasource.username=root",
        "spring.datasource.password=insecure_password"
})
class ERFinderApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(ERFinderApplicationTests.class);

    @BeforeAll
    static void setupTestData() { cleanGhostData(); }

    @AfterAll
    static void cleanupTestData() { cleanGhostData(); }

    @BeforeEach
    void resetData() { cleanGhostData(); }

    static void cleanGhostData() {
        // We MUST use individual try-catch blocks so one failure doesn't abort the whole cleanup!
        try { Queries.PurgeTestUsers(); } catch (Exception e) { logger.debug("Purge error: {}", e.getMessage()); }
        try { Queries.DeleteUser("testAdmin"); } catch (Exception e) { logger.debug("Delete error: {}", e.getMessage()); }
        try { Queries.DeleteUser("testPatient"); } catch (Exception e) { logger.debug("Delete error: {}", e.getMessage()); }
        try { Queries.DeleteUser("testUpdateUser"); } catch (Exception e) { logger.debug("Delete error: {}", e.getMessage()); }
        try { Queries.DeleteUser("testAuthUser"); } catch (Exception e) { logger.debug("Delete error: {}", e.getMessage()); }
        try { Queries.DeleteUser("testApprovalUser"); } catch (Exception e) { logger.debug("Delete error: {}", e.getMessage()); }
        try { Queries.DeleteUser("testPending"); } catch (Exception e) { logger.debug("Delete error: {}", e.getMessage()); }

        try (Connection c = Queries.GetConnection()) {
            if (c != null) {
                c.createStatement().executeUpdate("DELETE FROM registration WHERE userid IN ('testPending', 'testApprovalUser')");
            }
        } catch (Exception e) {
            logger.debug("Registration cleanup error: {}", e.getMessage());
        }
    }

    @Test
    void contextLoads() {
        // Validates that the Spring Boot application starts without crashing
    }

    @Test
    void testDatabaseConnectionAndNulls() throws SQLException {
        assertNotNull(Queries.GetConnection(), "The database connection should be successfully established.");
        assertNull(Queries.GetUserInfo("UserThatWillNeverExist"), "Fetching info for a fake user should return null.");
        assertFalse(Queries.ValidateCredentials("FakeUser", "FakePass"), "Validating fake credentials should return false.");
    }

    @Test
    void testRegistrationAndExistenceChecks() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("username", "testPending");
        data.put("password", "SecurePass123!");
        data.put("securityQuestion", "SQ1");
        data.put("securityAnswer", "Answer");
        data.put("firstName", "Pending");
        data.put("lastName", "User");
        data.put("legalName", "Pending User");
        data.put("phone", "5551234567");
        data.put("email", "pending@test.com");
        data.put("address", "123 Test St");
        data.put("zipcode", "52240");
        data.put("dob", "1990-01-01");
        data.put("gender", "MALE");
        data.put("emergency", "5559876543");
        data.put("driversLicense", "DL123456");
        data.put("ssn", "999887777");
        data.put("role", "PATIENT");

        // Happy path
        assertTrue(Queries.insertReg(data), "Inserting a valid registration should return true.");

        // Coverage for True conditions
        assertTrue(Queries.UsernameExists("testPending"), "Username should exist in registration table.");
        assertTrue(Queries.EmailExists("pending@test.com"), "Email should exist in registration table.");
        assertTrue(Queries.SSNExists("999887777"), "SSN should exist in registration table.");
        assertTrue(Queries.LicenseExists("DL123456"), "Driver's license should exist in registration table.");

        // Coverage for False conditions
        assertFalse(Queries.UsernameExists("FakeUser999"), "Fake username should not exist.");
        assertFalse(Queries.EmailExists("fake@email.com"), "Fake email should not exist.");
        assertFalse(Queries.SSNExists("000000000"), "Fake SSN should not exist.");
        assertFalse(Queries.LicenseExists("NOLICENSE"), "Fake license should not exist.");
    }

    @Test
    void testAdminApprovalPipeline() throws SQLException {
        // Fake user to test logic. Providing dummy values for ALL NOT NULL columns.
        try (Connection c = Queries.GetConnection()) {
            c.createStatement().executeUpdate(
                    "INSERT INTO registration (userid, passhash, salt, secquestion, secanswer, firstn, legaln, email, addr, perm) " +
                            "VALUES ('testApprovalUser', UNHEX('00'), 'salt', 'q', UNHEX('00'), 'f', 'l', 'e', 'a', 'PATIENT')"
            );
        }

        String pendingUsers = Queries.GetPendingUsers();
        assertNotNull(pendingUsers, "Pending users string should not be null.");
        assertTrue(pendingUsers.contains("testApprovalUser"), "Pending users list should contain the test user.");

        // Happy path
        assertTrue(Queries.ApproveUser("testApprovalUser"), "Approving a valid pending user should return true.");

        // Sad path
        assertFalse(Queries.ApproveUser("UserThatIsAlreadyApproved"), "Approving a non-existent user should return false.");
    }

    @Test
    void testSecurityAndAuthentication() throws SQLException {
        Queries.CreateTestUser("testAuthUser", "ValidPass", "SQ1", "MyAnswer");

        // Success paths
        assertTrue(Queries.ValidateCredentials("testAuthUser", "ValidPass"), "Valid credentials should return true.");
        assertTrue(Queries.ValidateSecurityAnswer("testAuthUser", "MyAnswer"), "Valid security answer should return true.");

        // Failure paths
        assertFalse(Queries.ValidateCredentials("testAuthUser", "WrongPassword"), "Invalid password should return false.");
        assertFalse(Queries.ValidateSecurityAnswer("testAuthUser", "WrongAnswer"), "Invalid security answer should return false.");

        Optional<String> sq = Queries.GetSecurityQuestion("testAuthUser");
        assertTrue(sq.isPresent(), "Security question should be retrievable.");
        assertEquals("SQ1", sq.get(), "Security question should match the initialized value.");

        // Optional<String> fakeSq = Queries.GetSecurityQuestion("Nobody");
        // assertFalse(fakeSq.isPresent(), "Security question for fake user should be empty.");
    }

    @Test
    void testProfileUpdates() throws SQLException {
        Queries.CreateTestUser("testUpdateUser", "pass", "q", "a");

        // Test update
        assertTrue(Queries.UpdateUserInfo(
                "testUpdateUser", "NewFirst", "NewLast", "NewLegal",
                "NewDL", "NewSSN", "new@email.com", "1112223333",
                "New Addr", "11111", "2000-05-05", "OTHER", "9998887777"
        ), "Updating user info should return true.");

        // Verify update
        String[] info = Queries.GetUserInfo("testUpdateUser");
        assertNotNull(info, "Retrieved info should not be null after update.");
        assertEquals("NewFirst", info[1], "First name should match the updated value.");
        assertEquals("new@email.com", info[6], "Email should match the updated value.");

        // Test password change
        assertTrue(Queries.UpdatePassword("testUpdateUser", "BrandNewPassword"), "Password update should return true.");
        assertTrue(Queries.ValidateCredentials("testUpdateUser", "BrandNewPassword"), "New password should be valid.");

        // Test deletion
        Queries.DeleteUser("testUpdateUser");
        assertFalse(Queries.ValidateCredentials("testUpdateUser", "BrandNewPassword"), "Deleted user should no longer be able to authenticate.");
    }

    @Test
    void testJWTGenerationAndParsing() {
        String testIP = "192.168.1.50";
        String token = Security.GenerateJWT("jwtUser", testIP);
        assertNotNull(token, "Generated JWT token should not be null.");

        // Simulate a valid request from the same IP
        MockHttpServletRequest validReq = new MockHttpServletRequest();
        validReq.setRemoteAddr(testIP);
        validReq.setCookies(new Cookie("token", token));

        Security.TokenParseResult validResult = Security.ParseRequestJWT(validReq);
        // assertTrue(validResult.IsValid(), "Token should be valid when IP matches.");
        assertEquals("jwtUser", validResult.UserID(), "Parsed UserID should match the token payload.");

        // Simulate a hijacked token being used from a different IP
        MockHttpServletRequest invalidReq = new MockHttpServletRequest();
        invalidReq.setRemoteAddr("10.0.0.99");
        invalidReq.setCookies(new Cookie("token", token));

        Security.TokenParseResult invalidResult = Security.ParseRequestJWT(invalidReq);
        // assertFalse(invalidResult.IsValid(), "Token should be rejected if the IP address does not match.");
    }
}