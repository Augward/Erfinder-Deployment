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

    private static Connection TestSQLConnection;
    private static Connection GetConnection() {
        if (TestSQLConnection != null)
        {
            return TestSQLConnection;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
                    .getDeclaredConstructor().newInstance();
            // this should be file-driven. But one step at a time!
            TestSQLConnection = DriverManager.getConnection(
                    "jdbc:mysql://localhost/erfinder?" +
                            "user=root&password=insecure_password");
            return TestSQLConnection;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            assert (false);
        } catch (Exception e) {
            assert(false);
        }
        return null;
    }
    private void PrintSQLException(SQLException ex) {
        System.out.println("SQLException: " + ex.getMessage());
        System.out.println("SQLState: " + ex.getSQLState());
        System.out.println("VendorError: " + ex.getErrorCode());
    }
    @Test
    void establishDBConnection() {
        GetConnection();
    }

    // tries to get a user w/ id = 1 in the 'users' table.
    @Test
    void getTestEntry() {
        Connection connection = GetConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * from users WHERE id = 1;");


        } catch (SQLException ex) {
            PrintSQLException(ex);
            assert (false);
        } catch (Exception e) {
            assert(false);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    PrintSQLException(ex);
                    assert(false);
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                    stmt = null;
                } catch (SQLException ex){
                    PrintSQLException(ex);
                    assert(false);
                }
            }
        }
    }
}
