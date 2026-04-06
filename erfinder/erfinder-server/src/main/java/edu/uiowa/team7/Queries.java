package edu.uiowa.team7;

import java.sql.*;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class Queries {

    private static Connection SQLConnection;

    public static Connection GetConnection() {
        if (SQLConnection != null) {
            try {
                if (!SQLConnection.isClosed()) {
                    return SQLConnection;
                }
            } catch (Exception ignored) {}
        }
        try {
            String dbHost = System.getenv("DB_HOST");
            if (dbHost == null || dbHost.trim().isEmpty()) {
                dbHost = "localhost";
            }

            String dbPass = System.getenv("MYSQL_ROOT_PASSWORD");
            if (dbPass == null || dbPass.trim().isEmpty()) {
                dbPass = "insecure_password";
            }

            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            SQLConnection = DriverManager.getConnection("jdbc:mysql://" + dbHost + "/erfinder?user=root&password=" + dbPass);
            return SQLConnection;
        } catch (SQLException ex) {
            PrintSQLException(ex);
            throw new RuntimeException("Database connection failed", ex);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during connection", e);
        }
    }

    public static void PrintSQLException(SQLException e) {
        System.out.println("SQLException: " + e.getMessage());
        System.out.println("SQLState: " + e.getSQLState());
        System.out.println("VendorError: " + e.getErrorCode());
    }


    // Helper Class for Authentication
    public static class AuthResult {
        public final boolean isValid;
        public final String status;
        public AuthResult(boolean isValid, String status) {
            this.isValid = isValid;
            this.status = status;
        }
    }

    // Authentication & Security
    public static AuthResult ValidateCredentials(String userID, String password) throws SQLException {
        try(Connection c = GetConnection(); PreparedStatement stmt = c.prepareStatement("CALL ValidatePassword(?,?);")) {
            stmt.setString(1, userID);
            stmt.setString(2, password);
            if (stmt.execute()) {
                try (ResultSet r = stmt.getResultSet()) {
                    if (r.next()) {
                        return new AuthResult(r.getBoolean("is_valid"), r.getString("status"));
                    }
                }
            }
            return new AuthResult(false, null);
        }
    }

    public static boolean ValidateSecurityAnswer(String userID, String answer) throws SQLException, NullPointerException {
        try(Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement("CALL ValidateSecurityAnswer(?,?);");
            stmt.setString(1, userID);
            stmt.setString(2, answer);
            if (stmt.execute()) {
                try (ResultSet r = stmt.getResultSet()) {
                    if (r.next()) return r.getBoolean(1);
                }
            }
            return false;
        }
    }

    public static Optional<String> GetSecurityQuestion(String userID) throws SQLException, NullPointerException {
        try (Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement("CALL GetSecurityQuestion(?);");
            stmt.setString(1, userID);
            if (stmt.execute()) {
                try (ResultSet r = stmt.getResultSet()) {
                    if (r.next()) return Optional.of(r.getString(1));
                }
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

    public static String[] GetUserInfo(String userID) throws SQLException {
        try(Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement(
                    "SELECT CASE WHEN status = 'PENDING' THEN 'PENDING' ELSE perm END as display_role, " +
                            "firstn, lastn, legaln, dln, ssn, email, phone, addr, zip, dob, gender, contact " +
                            "FROM users WHERE userid = ?");
            stmt.setString(1, userID);
            try (ResultSet r = stmt.executeQuery()) {
                if (r.next()) {
                    String[] info = new String[13];
                    for(int i = 0; i < 13; i++) {
                        String val = r.getString(i + 1);
                        info[i] = (val == null) ? "" : val;
                    }
                    return info;
                }
            }
            return null;
        }
    }

    public static boolean UpdateUserInfo(String userID, String firstn, String lastn, String legaln, String dln, String ssn, String email, String phone, String addr, String zip, String dob, String gender, String contact) throws SQLException {
        try(Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement(
                    "UPDATE users SET firstn=?, lastn=?, legaln=?, dln=?, ssn=?, email=?, phone=?, addr=?, zip=?, dob=?, gender=?, contact=? WHERE userid=?");
            stmt.setString(1, firstn); stmt.setString(2, lastn); stmt.setString(3, legaln);
            stmt.setString(4, dln); stmt.setString(5, ssn); stmt.setString(6, email);
            stmt.setString(7, phone); stmt.setString(8, addr); stmt.setString(9, zip);
            stmt.setString(10, dob); stmt.setString(11, gender); stmt.setString(12, contact);
            stmt.setString(13, userID);
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean UpdatePassword(String userID, String newPassword) throws SQLException {
        try(Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement(
                    "UPDATE users SET passhash = UNHEX(SHA2(CONCAT(?, salt), 256)) WHERE userid = ?"
            );
            stmt.setString(1, newPassword);
            stmt.setString(2, userID);
            return stmt.executeUpdate() > 0;
        }
    }

    public static void DeleteUser(String userID) throws SQLException {
        try(Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement("DELETE FROM users WHERE userid = ?");
            stmt.setString(1, userID);
            stmt.executeUpdate();
        }
    }

    public static String GetPendingUsers() throws SQLException {
        try(Connection c = GetConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT userid, perm FROM users WHERE status = 'PENDING'");
            ResultSet r = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (r.next()) {
                sb.append(r.getString("userid")).append(":").append(r.getString("perm")).append(",");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }

    public static boolean ApproveUser(String targetUserID) throws SQLException {
        try(Connection c = GetConnection(); PreparedStatement stmt = c.prepareStatement(
                "UPDATE users SET status = 'APPROVED' WHERE userid = ? AND status = 'PENDING'")) {
            stmt.setString(1, targetUserID);
            return stmt.executeUpdate() > 0;
        }
    }

    // Simplified Uniqueness Checks
    public static boolean FieldExists(String field, String value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE " + field + " = ?";
        try(Connection conn = GetConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public static boolean UsernameExists(String username) throws Exception{
        String sql = "SELECT (SELECT COUNT(*) FROM users WHERE userid = ?) + " +
                "(SELECT COUNT(*) FROM registration WHERE userid = ?)";
        try(Connection conn = GetConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public static boolean EmailExists(String email) throws Exception{
        String sql = "SELECT (SELECT COUNT(*) FROM users WHERE email = ?) + " +
                "(SELECT COUNT(*) FROM registration WHERE email = ?)";
        try(Connection conn = GetConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, email);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1)> 0;
        }
    }

    public static boolean SSNExists(String ssn) throws Exception{
        String sql = "SELECT (SELECT COUNT(*) FROM users WHERE ssn = ?) + " +
                "(SELECT COUNT(*) FROM registration WHERE ssn = ?)";
        try(Connection conn = GetConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, ssn);
            stmt.setString(2, ssn);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public static boolean LicenseExists(String license) throws Exception{
        String sql = "SELECT (SELECT COUNT(*) FROM users WHERE dln = ?) + " +
                "(SELECT COUNT(*) FROM registration WHERE dln = ?)";
        try(Connection conn = GetConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1,license);
            stmt.setString(2, license);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public static boolean insertReg(Map<String, String> data) throws SQLException{
        String salt = java.util.UUID.randomUUID().toString().substring(0, 8);
        String sql = "INSERT INTO users (status, userid, passhash, salt, secquestion, secanswer, firstn, lastn, legaln, phone, email, addr, zip, dob, gender, contact, dln, ssn, perm) " +
                "VALUES ('PENDING', ?, UNHEX(SHA2(CONCAT(?, ?), 256)), ?, ?, UNHEX(SHA2(CONCAT(?, ?), 256)), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = GetConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, data.get("username"));
            ps.setString(2, data.get("password"));
            ps.setString(3, salt);
            ps.setString(4, salt);
            ps.setString(5, data.get("securityQuestion"));
            ps.setString(6, data.get("securityAnswer"));
            ps.setString(7, salt);
            ps.setString(8, data.get("firstName"));
            ps.setString(9, data.get("lastName"));
            ps.setString(10, data.get("legalName"));
            ps.setString(11, data.get("phone"));
            ps.setString(12, data.get("email"));
            ps.setString(13, data.get("address"));
            ps.setString(14, data.get("zipcode"));
            ps.setDate(15, java.sql.Date.valueOf(data.get("dob")));
            ps.setString(16, data.get("gender"));
            ps.setString(17, data.get("emergency"));
            ps.setString(18, data.get("driversLicense"));
            ps.setString(19, data.get("ssn"));
            ps.setString(20, data.get("role"));
            return ps.executeUpdate() > 0;
        } catch(SQLException e){
            PrintSQLException(e);
            throw e;
        }
    }
}