package edu.uiowa.team7;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "erfinder.login.max-attempts-in-period=10",
        "erfinder.login.clear-attempt-after-minutes=30",
        "erfinder.login.timeout-period-minutes=300"
})
class ERFinderApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(ERFinderApplicationTests.class);

    @BeforeAll
    static void setupTestData() { cleanGhostData(); }

    @BeforeEach
    void resetData() { cleanGhostData(); }

    @AfterAll
    static void cleanupTestData() { cleanGhostData(); }

    static void cleanGhostData() {
        try { Queries.PurgeTestUsers(); } catch (Exception ignored) {}
        try { Queries.DeleteUser("testAdmin"); } catch (Exception ignored) {}
        try { Queries.DeleteUser("testPatient"); } catch (Exception ignored) {}
        try { Queries.DeleteUser("testUpdateUser"); } catch (Exception ignored) {}
        try { Queries.DeleteUser("testAuthUser"); } catch (Exception ignored) {}
        try { Queries.DeleteUser("testApprovalUser"); } catch (Exception ignored) {}
        try { Queries.GetConnection().createStatement().executeUpdate("DELETE FROM registration WHERE userid='testPending'"); } catch (Exception ignored) {}
        try { Queries.GetConnection().createStatement().executeUpdate("DELETE FROM registration WHERE userid='testApprovalUser'"); } catch (Exception ignored) {}
    }

    @Test
    void testDatabaseConnectionAndNulls() throws SQLException {
        assertNotNull(Queries.GetConnection());
        assertNull(Queries.GetUserInfo("UserThatWillNeverExist"));
        assertFalse(Queries.ValidateCredentials("FakeUser", "FakePass"));
    }

    @Test
    void testRegistrationAndExistenceChecks() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("username", "testPending"); data.put("password", "SecurePass123!");
        data.put("securityQuestion", "SQ1"); data.put("securityAnswer", "Answer");
        data.put("firstName", "Pending"); data.put("lastName", "User");
        data.put("legalName", "Pending User"); data.put("phone", "5551234567");
        data.put("email", "pending@test.com"); data.put("address", "123 Test St");
        data.put("zipcode", "52240"); data.put("dob", "1990-01-01");
        data.put("gender", "MALE"); data.put("emergency", "5559876543");
        data.put("driversLicense", "DL123456"); data.put("ssn", "999887777");
        data.put("role", "PATIENT");

        // Happy path
        assertTrue(Queries.insertReg(data));

        // Coverage for True conditions
        assertTrue(Queries.UsernameExists("testPending"));
        assertTrue(Queries.EmailExists("pending@test.com"));
        assertTrue(Queries.SSNExists("999887777"));
        assertTrue(Queries.LicenseExists("DL123456"));

        // Coverage for False conditions
        assertFalse(Queries.UsernameExists("FakeUser999"));
        assertFalse(Queries.EmailExists("fake@email.com"));
        assertFalse(Queries.SSNExists("000000000"));
        assertFalse(Queries.LicenseExists("NOLICENSE"));
    }

    @Test
    void testAdminApprovalPipeline() throws SQLException {
        // Fake user to test logic
        try {
            Queries.GetConnection().createStatement().executeUpdate(
                    "INSERT INTO registration (userid, perm, email, firstn, legaln, addr) VALUES ('testApprovalUser', 'PATIENT', 'e', 'f', 'l', 'a')"
            );
        } catch (Exception ignored) {}

        String pendingUsers = Queries.GetPendingUsers();
        assertNotNull(pendingUsers);

        // Happy path
        assertTrue(Queries.ApproveUser("testApprovalUser"));

        // Sad path
        assertFalse(Queries.ApproveUser("UserThatIsAlreadyApproved"));
    }

    @Test
    void testSecurityAndAuthentication() throws SQLException {
        Queries.CreateTestUser("testAuthUser", "ValidPass", "SQ1", "MyAnswer");

        // Success paths
        assertTrue(Queries.ValidateCredentials("testAuthUser", "ValidPass"));
        assertTrue(Queries.ValidateSecurityAnswer("testAuthUser", "MyAnswer"));

        Optional<String> sq = Queries.GetSecurityQuestion("testAuthUser");
        assertTrue(sq.isPresent());
        assertEquals("SQ1", sq.get());

        // Failure paths
        assertFalse(Queries.ValidateCredentials("testAuthUser", "WrongPassword"));
        assertFalse(Queries.ValidateSecurityAnswer("testAuthUser", "WrongAnswer"));

        Optional<String> fakeSq = Queries.GetSecurityQuestion("Nobody");
        assertFalse(fakeSq.isPresent());
    }

    @Test
    void testProfileUpdates() throws SQLException {
        Queries.CreateTestUser("testUpdateUser", "pass", "q", "a");

        // Test update
        assertTrue(Queries.UpdateUserInfo(
                "testUpdateUser", "NewFirst", "NewLast", "NewLegal",
                "NewDL", "NewSSN", "new@email.com", "1112223333",
                "New Addr", "11111", "2000-05-05", "OTHER", "9998887777"
        ));

        // Verify update
        String[] info = Queries.GetUserInfo("testUpdateUser");
        assertNotNull(info);
        assertEquals("NewFirst", info[1]);
        assertEquals("new@email.com", info[6]);

        // Test password change
        assertTrue(Queries.UpdatePassword("testUpdateUser", "BrandNewPassword"));
        assertTrue(Queries.ValidateCredentials("testUpdateUser", "BrandNewPassword"));

        // Test deletion
        Queries.DeleteUser("testUpdateUser");
        assertFalse(Queries.ValidateCredentials("testUpdateUser", "BrandNewPassword"));
    }

    @Test
    void testJWTGenerationAndParsing() {
        String token = Security.GenerateJWT("jwtUser", "device123");
        assertNotNull(token);

        Security.TokenParseResult validResult = Security.ParseRequestJWT(token, "device123");
        assertTrue(validResult.IsValid());
        assertEquals("jwtUser", validResult.UserID());

        Security.TokenParseResult invalidResult = Security.ParseRequestJWT(token, "wrongDevice");
        assertFalse(invalidResult.IsValid());
    }
}