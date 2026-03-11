package edu.uiowa.team7;

import java.sql.*;
import java.util.Optional;

public class Queries {

    private static Connection SQLConnection;

    public static Connection GetConnection() {
        if (SQLConnection != null)
        {
            try {
                if (!SQLConnection.isClosed()) {
                    return SQLConnection;
                }
            } catch (Exception ignored) {}
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
                    .getDeclaredConstructor().newInstance();
            // this should be file-driven. But one step at a time!
            SQLConnection = DriverManager.getConnection(
                    "jdbc:mysql://localhost/erfinder?" +
                            "user=root&password=insecure_password");
            return SQLConnection;
        } catch (SQLException ex) {
            PrintSQLException(ex);
            assert (false);
        } catch (Exception e) {
            assert(false);
        }
        return null;
    }

    public static void PrintSQLException(SQLException e) {
        System.out.println("SQLException: " + e.getMessage());
        System.out.println("SQLState: " + e.getSQLState());
        System.out.println("VendorError: " + e.getErrorCode());
    }

    public static boolean ValidateCredentials(String userID, String password)
            throws SQLException, NullPointerException {
        try(Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement("CALL ValidatePassword(?,?);");
            stmt.setString(1, userID);
            stmt.setString(2, password);
            if (stmt.execute()) {
                ResultSet r = stmt.getResultSet();
                r.next();
                boolean valid = r.getBoolean(1);
                r.close();
                stmt.close();
                return valid;
            }
            return false;
        }
    }

    public static boolean ValidateSecurityAnswer(String userID, String answer)
            throws SQLException, NullPointerException {
        try(Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement("CALL ValidateSecurityAnswer(?,?);");
            stmt.setString(1, userID);
            stmt.setString(2, answer);
            if (stmt.execute()) {
                ResultSet r = stmt.getResultSet();
                r.next();
                boolean valid = r.getBoolean(1);
                r.close();
                stmt.close();
                return valid;
            }
            return false;
        }
    }

    public static Optional<String> GetSecurityQuestion(String userID)
            throws SQLException, NullPointerException {
        try (Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement("CALL GetSecurityQuestion(?);");
            stmt.setString(1, userID);
            if (stmt.execute()) {
                ResultSet r = stmt.getResultSet();
                r.next();
                String question = r.getString(1);
                r.close();
                stmt.close();
                return Optional.of(question);
            }
        }
        return Optional.empty();
    }

    public static void PurgeTestUsers() throws SQLException {
        try(Connection c = GetConnection()) {
            c.createStatement().executeQuery("CALL PurgeTestUsers();").close();
        }
    }

    public static void CreateTestUser(String userID, String password, String secq, String seca) throws SQLException{
        try(Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement("CALL CreateTestUser(?,?,?,?);");
            stmt.setString(1, userID);
            stmt.setString(2, password);
            stmt.setString(3, secq);
            stmt.setString(4, seca);
            stmt.executeQuery();
            stmt.close();
        }
    }
}
