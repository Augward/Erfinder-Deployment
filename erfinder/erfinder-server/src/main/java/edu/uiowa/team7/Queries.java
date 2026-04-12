package edu.uiowa.team7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.*;
import java.sql.Date;
import java.util.*;

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

            String dbHost = System.getenv("DB_HOST");
            if (dbHost == null || dbHost.trim().isEmpty()) {
                dbHost = "localhost";
            }

            String dbPass = System.getenv("MYSQL_ERFINDER_PASSWORD");
            if (dbPass == null || dbPass.trim().isEmpty()) {
                dbPass = "insecure_password";
            }

            String dbUser = System.getenv("MYSQL_ERFINDER_USER");
            if (dbUser == null || dbUser.trim().isEmpty()) {
                dbUser = "root";
            }
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            SQLConnection = DriverManager.getConnection("jdbc:mysql://" + dbHost + "/erfinder?user="+ dbUser + "&password=" + dbPass);

            //String url = "jdbc:mysql://" + "erfinder-erfinder.k.aivencloud.com" + ":" + "26268" + "/" + "erfinder" + "?sslMode=REQUIRED";
            //SQLConnection = DriverManager.getConnection(url, "avnadmin", "AVNS_2kwhLT7ZoRaVQ6GpXiz");

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

    // Helper Class for Authentication
    public static class AuthResult {
        public final boolean isValid;
        public final String status;
        public AuthResult(boolean isValid, String status) {
            this.isValid = isValid;
            this.status = status;
        }
    }

    public static AuthResult ValidateCredentials(String userID, String password) throws SQLException {
        try (
                Connection c = GetConnection();
                CallableStatement stmt = c.prepareCall("CALL ValidatePassword(?,?);")
        ) {
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

    public static boolean ValidateSecurityAnswer(
            String userID, String answer
    ) throws SQLException {
        try (
                Connection c = GetConnection();
                CallableStatement stmt = c.prepareCall(
                        "CALL ValidateSecurityAnswer(?,?);"
                )
        ) {
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
        try (
                Connection c = GetConnection();
                CallableStatement stmt = c.prepareCall(
                     "SELECT CASE WHEN status = 'PENDING' THEN 'PENDING' ELSE perm END as display_role, " +
                             "firstn, lastn, legaln, dln, ssn, email, phone, addr, zip, dob, gender, contact " +
                             "FROM users WHERE userid = ?")
        ) {
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
             CallableStatement stmt = c.prepareCall("SELECT userid, perm FROM users WHERE status = 'PENDING'");
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
        try(Connection c = GetConnection(); PreparedStatement stmt = c.prepareStatement(
                "UPDATE users SET status = 'APPROVED' WHERE userid = ? AND status = 'PENDING'")) {
            stmt.setString(1, targetUserID);
            return stmt.executeUpdate() > 0;
        }
    }
    // Simplified Uniqueness Checks
    // We can use string append here, as 'field' is determined server-side only.
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
    // todo:: remove these *Exists() functions, replace with FieldExists()

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
            CallableStatement stmt = conn.prepareCall("CALL add_facility(?,?,?,?,?,?,?,?,?,?)")){

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

    public static boolean UpdateFacility(int facilityId, int beds, int waitTime) throws SQLException{
        try(Connection c = GetConnection(); CallableStatement stmt = c.prepareCall("CALL update_facility(?,?,?)")){
            stmt.setInt(1, facilityId);
            stmt.setInt(2, beds);
            stmt.setInt(3, waitTime);

            return stmt.executeUpdate() > 0;
        }

    }

    public static String GetAllFacilitiesJSON() throws SQLException {

        String sql =
                "SELECT id, er_name, latitude, longitude, trauma_level, " +
                        "specialties, bed_availability, waitTime_Minutes " +
                        "FROM facilities";

        try (Connection c = GetConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            StringBuilder json = new StringBuilder("[");
            while (rs.next()) {
                json.append("{")
                        .append("\"id\":").append(rs.getInt("id")).append(",")
                        .append("\"name\":\"").append(rs.getString("er_name")).append("\",")
                        .append("\"lat\":").append(rs.getDouble("latitude")).append(",")
                        .append("\"lon\":").append(rs.getDouble("longitude")).append(",")
                        .append("\"trauma\":\"").append(rs.getString("trauma_level")).append("\",")
                        .append("\"specialties\":\"").append(rs.getString("specialties")).append("\",")
                        .append("\"beds\":").append(rs.getInt("bed_availability")).append(",")
                        .append("\"wait\":").append(rs.getInt("waitTime_Minutes"))
                        .append("},");
            }

            if (json.length() > 1) {
                json.setLength(json.length() - 1); // remove trailing comma
            }
            json.append("]");
            return json.toString();
        }
    }

    public static boolean UpdateWaitTime(int facilityId, int waitTime) throws SQLException{
        String sql = "{CALL updateWaitTime(?,?)}";

        try(Connection conn = GetConnection(); CallableStatement stmt = conn.prepareCall(sql)){
            stmt.setInt(1, facilityId);
            stmt.setInt(2, waitTime);

            return stmt.executeUpdate() > 0;
        }
    }

    public static String GetInsurances(String userid) throws SQLException {
        try (
                Connection c = GetConnection();
                CallableStatement stmt = c.prepareCall(
                        "SELECT * FROM insurances WHERE userid = ?"
                )
        ) {
            // get all the member ids associated with the userid:
            stmt.setString(1, userid);
            try (ResultSet rs = stmt.executeQuery()) {
                StringBuilder s = new StringBuilder();
                boolean first = true;
                while (rs.next()) {
                    if (first) {
                        first = false;
                    } else {
                        s.append("|");
                    }
                    s.append("member_id:");
                    s.append(B64Encode(rs.getString("member_id")));
                    s.append(",");
                    // most of these are optional, so they get null-checks.
                    AppendStrProperty(rs, "insurer_name", s);
                    AppendIntProperty(rs, "policy_num", s);
                    AppendIntProperty(rs, "group_num", s);
                    AppendDecProperty(rs, "copay", s);
                    AppendDecProperty(rs, "coinsurance", s);
                    AppendDecProperty(rs, "out_of_pocket_max", s);
                    AppendStrProperty(rs, "plan_type", s);
                    AppendStrProperty(rs, "rx_bin", s);
                    AppendStrProperty(rs, "rx_pcn", s);
                    AppendStrProperty(rs, "rx_group", s);
                    AppendStrProperty(rs, "rx_id", s);
                    AppendStrProperty(rs, "customer_service_phone", s);
                    Date start = rs.getDate("effective_date");
                    if (!rs.wasNull()) {
                        s.append("effective_date:");
                        s.append(B64Encode(start.toString()));
                    }
                }
                return s.toString();
            }
        }
    }

    private static String B64Encode(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static void AppendIntProperty(ResultSet rs, String prop, StringBuilder s) throws SQLException{
        int value = rs.getInt(prop);
        if (!rs.wasNull()) {
            s.append(prop);
            s.append(":");
            s.append(value);
            s.append(",");
        }
    }
    private static void AppendDecProperty(ResultSet rs, String prop, StringBuilder s) throws SQLException{
        BigDecimal value = rs.getBigDecimal(prop);
        if (!rs.wasNull()) {
            s.append(prop);
            s.append(":");
            s.append(value.multiply(BigDecimal.valueOf(100)).intValue());
            s.append(",");
        }
    }

    private static void AppendStrProperty(ResultSet rs, String prop, StringBuilder s) throws  SQLException {
        String value = rs.getString(prop);
        if (!rs.wasNull()) {
            s.append(prop);
            s.append(":");
            s.append(B64Encode(value));
            s.append(",");
        }
    }


    enum ParamType {
        Str,
        Int,
        Dec
    }

    public static void SetInsurance(String userid, String blob, boolean fresh) throws Exception {
        StringBuilder s = new StringBuilder();
        s.append("UPDATE insurances SET ");

        // determine what's here...
        String[] pairs = blob.split(",");
        HashMap<String,String> map = new HashMap<>();
        for (String p : pairs) {
            String[] div = p.split(":");
            map.put(div[0],div[1]);
        }
        if (!map.containsKey("member_id")) {
            throw new Exception("Blob didn't contain full primary key");
        }
        String member_id = StatusController.B64Decode(map.get("member_id"));
        map.remove("member_id");

        if (fresh) {
            if (!map.containsKey("insurer_name")) {
                throw new Exception("Fresh insurance doesn't have provider!");
            }
            String insurer_name = StatusController.B64Decode(map.get("insurer_name"));
            // create the row
            try (
                    Connection c = GetConnection();
                    CallableStatement stmt0 = c.prepareCall("INSERT INTO insurances (insurer_name, member_id, userid) VALUES (?,?,?);");
            ) {
                stmt0.setString(1,insurer_name);
                stmt0.setString(2,member_id);
                stmt0.setString(3,userid);
                stmt0.execute();
            }
        }

        // go through our expected parameters...

        List<ParamType> params = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        AddStrProperty(map, "insurer_name", s, params, values);
        AddStrProperty(map, "plan_type", s, params, values);
        AddStrProperty(map, "rx_bin", s, params, values);
        AddStrProperty(map, "rx_pcn", s, params, values);
        AddStrProperty(map, "rx_group", s, params, values);
        AddStrProperty(map, "rx_id", s, params, values);
        AddStrProperty(map, "customer_service_phone", s, params, values);
        AddStrProperty(map, "effective_date", s, params, values);

        AddIntProperty(map, "policy_num", s, params, values);
        AddIntProperty(map, "group_num", s, params, values);

        AddDecimalProperty(map, "copay", s, params, values);
        AddDecimalProperty(map, "coinsurance", s, params, values);
        AddDecimalProperty(map, "out_of_pocket_max", s, params, values);

        // need to remove the last comma
        if (!params.isEmpty()) {
            s.deleteCharAt(s.length() - 1);
            s.deleteCharAt(s.length() - 1);
        }
        //`insurer_name` = ?, `copay` = '30.13', `coinsurance` = '.15', `out_of_pocket_max` = '122.23' " +


        s.append("WHERE (member_id = ?) and (userid = ?)");

        logger.error(s.toString());

        try (
                Connection c = GetConnection();
                CallableStatement stmt = c.prepareCall(s.toString());
        ) {
            logger.error(""+stmt.getParameterMetaData().getParameterCount());
            int i = 1;
            for (; i <= params.size(); i++) {
                switch (params.get(i-1)) {
                    case Str:
                        stmt.setString(i,(String) values.get(i-1));
                        break;
                    case Int:
                        stmt.setInt(i,(int) values.get(i-1));
                        break;
                    case Dec:
                        stmt.setBigDecimal(i,(BigDecimal) values.get(i-1));
                        break;
                }
            }
            stmt.setString(i++,member_id);
            stmt.setString(i, userid);
            stmt.execute();
        }
    }
    private static void AddStrProperty(
            HashMap<String,String> map, String prop, StringBuilder s,
            List<ParamType> params, List<Object> values
    ) {
        if (map.containsKey(prop)) {
            s.append(prop);
            s.append(" = ?, ");
            params.add(ParamType.Str);
            values.add(StatusController.B64Decode(map.get(prop)));
        }
    }
    private static void AddIntProperty(
            HashMap<String,String> map, String prop, StringBuilder s,
            List<ParamType> params, List<Object> values
    ) {
        if (map.containsKey(prop)) {
            s.append(prop);
            s.append(" = ?, ");
            params.add(ParamType.Int);
            values.add(Integer.parseInt(map.get(prop)));
        }
    }
    private static void AddDecimalProperty(
            HashMap<String,String> map, String prop, StringBuilder s,
            List<ParamType> params, List<Object> values
    ) {
        if (map.containsKey(prop)) {
            s.append(prop);
            s.append(" = ?, ");
            params.add(ParamType.Dec);
            values.add(BigDecimal.valueOf(Integer.parseInt(map.get(prop))).divide(BigDecimal.valueOf(100)));
        }
    }

    public static void DeleteInsurance(String userid, String member_id) throws SQLException {
        try (Connection c = GetConnection();
            CallableStatement stmt = c.prepareCall("DELETE FROM insurances WHERE (userid = ?) and (member_id = ?)");
        ) {
            stmt.setString(1, userid);
            stmt.setString(2,member_id);
            stmt.execute();
        }
    }


}