package edu.uiowa.team7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class Queries {

    private static final Logger logger = LoggerFactory.getLogger(Queries.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static Connection SQLConnection;

    public static Connection GetConnection() {
        try {
            if (SQLConnection != null && !SQLConnection.isClosed()) {
                return SQLConnection;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = "jdbc:mysql://" + "erfinder-erfinder.k.aivencloud.com" + ":" + "26268" + "/" + "erfinder" + "?sslMode=REQUIRED";

            SQLConnection = DriverManager.getConnection(url, "avnadmin", "AVNS_2kwhLT7ZoRaVQ6GpXiz");

            return SQLConnection;

        } catch (SQLException e) {
            PrintSQLException(e);
            throw new RuntimeException("Database Connection Failed", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected Error During Connection", e);
        }
    }


    public static void PrintSQLException(SQLException e) {
        logger.error("SQLException: {}", e.getMessage());
        logger.error("SQLState: {}", e.getSQLState());
        logger.error("VendorError: {}", e.getErrorCode());
    }

    public static boolean ValidateCredentials(String userID, String password) throws SQLException {
        try (Connection c = GetConnection();
             CallableStatement stmt = c.prepareCall("CALL ValidatePassword(?,?);")) {
            stmt.setString(1, userID);
            stmt.setString(2, password);
            stmt.execute();
            ResultSet r = stmt.getResultSet();

            if (r != null && r.next()) {
                return r.getBoolean(1);
            }

            return false;
        }
    }

    public static boolean ValidateSecurityAnswer(String userID, String answer) throws SQLException {
        try (Connection c = GetConnection();
             CallableStatement stmt = c.prepareCall("CALL ValidateSecurityAnswer(?,?);")) {
            stmt.setString(1, userID);
            stmt.setString(2, answer);
            stmt.execute();
            ResultSet r = stmt.getResultSet();

            if (r != null && r.next()) {
                return r.getBoolean(1);
            }

            return false;
        }
    }

    public static Optional<String> GetSecurityQuestion(String userID) throws SQLException {
        try (Connection c = GetConnection();
             CallableStatement stmt = c.prepareCall("CALL GetSecurityQuestion(?);")) {
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
        try (Connection c = GetConnection();
             Statement stmt = c.createStatement()) {
            stmt.executeUpdate("CALL PurgeTestUsers();");
        }
    }

    public static void CreateTestUser(String userID, String password, String secq, String seca) throws SQLException {
        try (Connection c = GetConnection();
             CallableStatement stmt = c.prepareCall("CALL CreateTestUser(?,?,?,?);")) {
            stmt.setString(1, userID);
            stmt.setString(2, password);
            stmt.setString(3, secq);
            stmt.setString(4, seca);
            stmt.executeUpdate();
        }
    }

    public static String[] GetUserInfo(String userID) throws SQLException {
        try (Connection c = GetConnection();
             CallableStatement stmt = c.prepareCall(
                     "SELECT perm, firstn, lastn, legaln, dln, ssn, email, phone, addr, zip, dob, gender, contact FROM users WHERE userid = ?")) {
            stmt.setString(1, userID);
            try (ResultSet r = stmt.executeQuery()) {
                if (r.next()) {
                    String[] info = new String[13];
                    for (int i = 0; i < 13; i++) {
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
        try (Connection c = GetConnection();
             CallableStatement stmt = c.prepareCall(
                     "UPDATE users SET firstn=?, lastn=?, legaln=?, dln=?, ssn=?, email=?, phone=?, addr=?, zip=?, dob=?, gender=?, contact=? WHERE userid=?")) {
            stmt.setString(1, firstn); stmt.setString(2, lastn); stmt.setString(3, legaln);
            stmt.setString(4, dln); stmt.setString(5, ssn); stmt.setString(6, email);
            stmt.setString(7, phone); stmt.setString(8, addr); stmt.setString(9, zip);
            stmt.setString(10, dob); stmt.setString(11, gender); stmt.setString(12, contact);
            stmt.setString(13, userID);
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean UpdatePassword(String userID, String newPassword) throws SQLException {
        try (Connection c = GetConnection();
             CallableStatement stmt = c.prepareCall(
                     "UPDATE users SET passhash = UNHEX(SHA2(CONCAT(?, salt), 256)) WHERE userid = ?")) {
            stmt.setString(1, newPassword);
            stmt.setString(2, userID);
            return stmt.executeUpdate() > 0;
        }
    }

    public static void DeleteUser(String userID) throws SQLException {
        try (Connection c = GetConnection();
             CallableStatement stmt = c.prepareCall("DELETE FROM users WHERE userid = ?")) {
            stmt.setString(1, userID);
            stmt.executeUpdate();
        }
    }

    public static String GetPendingUsers() throws SQLException {
        try (Connection c = GetConnection();
             CallableStatement stmt = c.prepareCall("SELECT userid, perm FROM registration");
             ResultSet r = stmt.executeQuery()) {
            StringBuilder sb = new StringBuilder();
            while (r.next()) {
                sb.append(r.getString("userid")).append(":").append(r.getString("perm")).append(",");
            }
            if (!sb.isEmpty()) sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }

    public static boolean ApproveUser(String targetUserID) throws SQLException {
        try (Connection c = GetConnection()) {
            try (CallableStatement insertStmt = c.prepareCall(
                    "INSERT INTO users (perm, salt, userid, passhash, firstn, lastn, legaln, dln, ssn, phone, contact, email, addr, zip, dob, gender, secquestion, secanswer) " +
                            "SELECT perm, salt, userid, passhash, firstn, lastn, legaln, dln, ssn, phone, contact, email, addr, zip, dob, gender, secquestion, secanswer " +
                            "FROM registration WHERE userid = ?")) {
                insertStmt.setString(1, targetUserID);
                int inserted = insertStmt.executeUpdate();

                if (inserted > 0) {
                    try (CallableStatement deleteStmt = c.prepareCall("DELETE FROM registration WHERE userid = ?")) {
                        deleteStmt.setString(1, targetUserID);
                        deleteStmt.executeUpdate();
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean UsernameExists(String username) throws Exception {
        String sql = "SELECT (SELECT COUNT(*) FROM users WHERE userid = ?) + (SELECT COUNT(*) FROM registration WHERE userid = ?)";
        try (Connection conn = GetConnection(); CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, username); stmt.setString(2, username);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next(); return rs.getInt(1) > 0;
            }
        }
    }

    public static boolean EmailExists(String email) throws Exception {
        String sql = "SELECT (SELECT COUNT(*) FROM users WHERE email = ?) + (SELECT COUNT(*) FROM registration WHERE email = ?)";
        try (Connection conn = GetConnection(); CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, email); stmt.setString(2, email);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next(); return rs.getInt(1) > 0;
            }
        }
    }

    public static boolean SSNExists(String ssn) throws Exception {
        String sql = "SELECT (SELECT COUNT(*) FROM users WHERE ssn = ?) + (SELECT COUNT(*) FROM registration WHERE ssn = ?)";
        try (Connection conn = GetConnection(); CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, ssn); stmt.setString(2, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next(); return rs.getInt(1) > 0;
            }
        }
    }

    public static boolean LicenseExists(String license) throws Exception {
        String sql = "SELECT (SELECT COUNT(*) FROM users WHERE dln = ?) + (SELECT COUNT(*) FROM registration WHERE dln = ?)";
        try (Connection conn = GetConnection(); CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, license); stmt.setString(2, license);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next(); return rs.getInt(1) > 0;
            }
        }
    }

    public static boolean insertReg(Map<String, String> data) throws SQLException {
        byte[] saltBytes = new byte[4];
        SECURE_RANDOM.nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : saltBytes) { sb.append(String.format("%02x", b)); }
        String salt = sb.toString();

        String sql = "INSERT INTO registration (userid, passhash, salt, secquestion, secanswer, firstn, lastn, legaln, phone, email, addr, zip, dob, gender, contact, dln, ssn, perm) " +
                "VALUES (?, UNHEX(SHA2(CONCAT(?, ?), 256)), ?, ?, UNHEX(SHA2(CONCAT(?, ?), 256)), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = GetConnection(); CallableStatement ps = conn.prepareCall(sql)) {
            ps.setString(1, data.get("username")); ps.setString(2, data.get("password"));
            ps.setString(3, salt); ps.setString(4, salt);
            ps.setString(5, data.get("securityQuestion")); ps.setString(6, data.get("securityAnswer"));
            ps.setString(7, salt); ps.setString(8, data.get("firstName"));
            ps.setString(9, data.get("lastName")); ps.setString(10, data.get("legalName"));
            ps.setString(11, data.get("phone")); ps.setString(12, data.get("email"));
            ps.setString(13, data.get("address")); ps.setString(14, data.get("zipcode"));
            ps.setDate(15, java.sql.Date.valueOf(data.get("dob"))); ps.setString(16, data.get("gender"));
            ps.setString(17, data.get("emergency")); ps.setString(18, data.get("driversLicense"));
            ps.setString(19, data.get("ssn")); ps.setString(20, data.get("role"));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            PrintSQLException(e);
            throw e;
        }
    }

    public static boolean AddFacility(
            String erName, String phone, String address, String zip, String traumaLevel,
            String specialties, int bedAvailability, int waitTime, double latitude, double longitude
    ) throws SQLException{
        try(Connection conn = GetConnection();
            CallableStatement stmt = conn.prepareCall("CALL add_facility(?,?,?,?,?,?,?,?,?,?,?)")){

            stmt.setString(1, erName);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setString(4, zip);
            stmt.setString(5, traumaLevel);
            stmt.setString(6, specialties);
            stmt.setInt(7, bedAvailability);
            stmt.setInt(8, waitTime);
            stmt.setDouble(9, latitude);
            stmt.setDouble(10, longitude);

            stmt.execute();
            return true;

        }
        catch(SQLException e){
            PrintSQLException(e);
            throw e;
        }
    }
}