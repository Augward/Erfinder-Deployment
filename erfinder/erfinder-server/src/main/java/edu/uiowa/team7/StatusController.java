package edu.uiowa.team7;

import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class StatusController {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(StatusController.class);

    // Tracks failed logins
    private static final java.util.concurrent.ConcurrentHashMap<String, Integer> failedLogins = new java.util.concurrent.ConcurrentHashMap<>();

    @Autowired
    private EmailService emailService;

    // Helpers
    public static String B64Decode(String encoded) {
        if (encoded == null) return "";
        // URL fix
        return new String(Base64.getDecoder().decode(encoded.replace(" ", "+")));
    }

    // System Check Endpoint
    @GetMapping(value = "/api/status", produces = "application/json")
    public String getStatus() {
        return "{\"message\": \"ER Finder Backend is live and connected to MySQL!\"}";
    }

    // Authentication Entry Point (Login)
    @GetMapping(value ="/api/gettoken")
    public void GetToken(HttpServletRequest req, HttpServletResponse res) {
        try {
            String userID = B64Decode(req.getParameter("userID"));
            String password = B64Decode(req.getParameter("password"));

            Queries.AuthResult auth = Queries.ValidateCredentials(userID, password);

            if (!auth.isValid) {
                // Email logic (3x Fails)
                int attempts = failedLogins.getOrDefault(userID, 0) + 1;
                failedLogins.put(userID, attempts);

                if (attempts >= 3) {
                    String targetEmail = Queries.GetUserEmail(userID);
                    if (targetEmail != null) {
                        emailService.sendEmail(targetEmail,
                                "ERFinder Security Alert",
                                "There have been 3 failed login attempts on your account. Please reset your password if you do not recognize this activity.");
                    }
                    // Reset counter
                    failedLogins.put(userID, 0);
                }

                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            failedLogins.remove(userID);

            String device = Security.GetDevice(req);
            res.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieFresh(userID, device));

            // Routing Backbone: Return 202 for Pending, 200 for Approved
            if ("PENDING".equalsIgnoreCase(auth.status)) {
                res.setStatus(HttpStatus.ACCEPTED.value()); // 202
            } else {
                res.setStatus(HttpStatus.OK.value()); // 200
            }

        } catch (Exception e) {
            logger.error("Error during GetToken authentication", e);
            res.getHeaders(HttpHeaders.SET_COOKIE).clear();
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Forgot Password - Question
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

    // Forgot Password - Answer
    @GetMapping("/api/userseca")
    public String GetTokenFromSecq(HttpServletRequest req, HttpServletResponse res) {
        String userID = B64Decode(req.getParameter("userid"));
        String ans = B64Decode(req.getParameter("answer"));
        try {
            // Check answer
            if (!Queries.ValidateSecurityAnswer(userID, ans)) {
                // Email logic
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

            // Answer is correct
            String tempToken = java.util.UUID.randomUUID().toString();
            res.setStatus(HttpStatus.OK.value());

            // Email
            String userEmail = Queries.GetUserEmail(userID);

            // Construct the reset link
            String resetLink = "http://localhost:8080/reset.html?t=" + tempToken + "&u=" + req.getParameter("userid");

            // Send the email
            emailService.sendEmail(userEmail,
                    "ERFinder Password Reset Request",
                    "A password reset was requested for your account. Click the link below to securely reset your password:\n\n" + resetLink);

            // Success message.
            return "Email Dispatched";

        } catch (Exception e) {
            logger.error("Error during security answer validation", e);
            res.getHeaders(HttpHeaders.SET_COOKIE).clear();
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return "Server Error";
        }
    }

    // Session Termination
    @GetMapping("/api/logout")
    public void Logout(HttpServletResponse res) {
        res.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieDelete());
        res.setStatus(HttpStatus.OK.value());
    }

    // JWT Verification Check
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
}