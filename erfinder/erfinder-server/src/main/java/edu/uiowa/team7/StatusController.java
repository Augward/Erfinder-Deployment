package edu.uiowa.team7;

import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// Status API Endpoints
@RestController
public class StatusController {

    // Component Logger
    private static final Logger logger = LoggerFactory.getLogger(StatusController.class);

    // Track Consecutive Failures
    private static final java.util.concurrent.ConcurrentHashMap<String, Integer> failedLogins = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    private EmailService emailService;

    // Base64 Decoding Utility
    public static String B64Decode(String encoded) {
        if (encoded == null || encoded.equals("null")) return "";

        // Fix Network Spaces
        String clean = encoded.replace(" ", "+");

        // Validate Required Byte Padding Block
        while (clean.length() % 4 != 0) {
            clean += "=";
        }

        return new String(Base64.getDecoder().decode(clean));
    }

    // System Health Check
    @GetMapping(value = "/api/status", produces = "application/json")
    public String getStatus() {
        return "{\"message\": \"ER Finder Backend is live and connected to MySQL!\"}";
    }

    // Authentication Entry Point Route
    @GetMapping(value ="/api/gettoken")
    public void GetToken(HttpServletRequest req, HttpServletResponse res) {
        try {
            String userID = B64Decode(req.getParameter("userID"));
            String password = B64Decode(req.getParameter("password"));

            Queries.AuthResult auth = Queries.ValidateCredentials(userID, password);

            if (!auth.isValid) {
                // Email Notification Logic
                int attempts = failedLogins.getOrDefault(userID, 0) + 1;
                failedLogins.put(userID, attempts);

                if (attempts >= 3) {
                    String targetEmail = Queries.GetUserEmail(userID);
                    if (targetEmail != null) {
                        emailService.sendEmail(targetEmail,
                                "ERFinder Security Alert",
                                "There have been 3 failed login attempts on your account. Please reset your password if you do not recognize this activity.");
                    }
                    failedLogins.put(userID, 0);
                }

                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            failedLogins.remove(userID);
            String device = Security.GetDevice(req);
            res.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieFresh(userID, device));

            // Process Verification Level Result Code
            if ("PENDING".equalsIgnoreCase(auth.status)) {
                res.setStatus(HttpStatus.ACCEPTED.value());
            } else {
                res.setStatus(HttpStatus.OK.value());
            }

        } catch (Exception e) {
            logger.error("Error during GetToken authentication", e);
            res.getHeaders(HttpHeaders.SET_COOKIE).clear();
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Fetch Password Question Check
    @GetMapping("/api/usersecq")
    public String GetSecurityQuestion(HttpServletRequest req, HttpServletResponse res) {
        try {
            String userid = B64Decode(req.getParameter("userid"));
            Optional<String> secq = Queries.GetSecurityQuestion(userid);
            if (secq.isEmpty()) {
                res.setStatus(HttpStatus.NOT_FOUND.value());
                return "";
            }
            return secq.get();
        } catch (Exception e) {
            logger.error("Error retrieving security question", e);
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return "";
        }
    }

    // Password Recovery Submission Flow
    @GetMapping("/api/userseca")
    public String GetTokenFromSecq(HttpServletRequest req, HttpServletResponse res) {
        String userID = B64Decode(req.getParameter("userid"));
        String ans = B64Decode(req.getParameter("answer"));

        try {
            if (!Queries.ValidateSecurityAnswer(userID, ans)) {
                try {
                    String targetEmail = Queries.GetUserEmail(userID);
                    if (targetEmail != null) {
                        emailService.sendEmail(targetEmail,
                                "ERFinder Security Alert",
                                "Someone recently failed a security question attempt on your account. If this was not you, please ensure your account is secure.");
                    }
                } catch (Exception ex) {
                    logger.error("Failed to send security alert", ex);
                }
                res.setStatus(HttpStatus.NOT_FOUND.value());
                return "Unauthorized";
            }

            // Generate Temporary Password Route Key
            String tempToken = java.util.UUID.randomUUID().toString();
            res.setStatus(HttpStatus.OK.value());

            String userEmail = Queries.GetUserEmail(userID);
            String resetLink = "http://localhost:8080/reset.html?t=" + tempToken + "&u=" + req.getParameter("userid");

            emailService.sendEmail(userEmail,
                    "ERFinder Password Reset Request",
                    "A password reset was requested for your account. Click the link below to securely reset your password:\n\n" + resetLink);

            return "Email Dispatched";

        } catch (Exception e) {
            logger.error("Error during security answer validation", e);
            res.getHeaders(HttpHeaders.SET_COOKIE).clear();
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return "Server Error";
        }
    }

    // Account Session Termination End
    @GetMapping("/api/logout")
    public void Logout(HttpServletResponse res) {
        res.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieDelete());
        res.setStatus(HttpStatus.OK.value());
    }

    // Session Verification Route Token
    @GetMapping("/api/username")
    public String GetUsernameInToken(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);

        if (result.Invalid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieDelete());
            return "-";
        }

        result.TryRefreshToken(request, response);
        return result.UserID();
    }

    // Email Password Reset Flow Target
    @GetMapping("/api/resetpassword")
    public void ResetPasswordFromEmail(HttpServletRequest request, HttpServletResponse response) {
        try {
            String newPassB64 = request.getParameter("newpass");
            String token = request.getParameter("t");
            String userIDB64 = request.getParameter("u");

            if (token == null || userIDB64 == null || newPassB64 == null) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return;
            }

            String userID = B64Decode(userIDB64);
            String newPass = B64Decode(newPassB64);

            Queries.UpdatePassword(userID, newPass);
            response.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {
            logger.error("FATAL ERROR during email password reset", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}