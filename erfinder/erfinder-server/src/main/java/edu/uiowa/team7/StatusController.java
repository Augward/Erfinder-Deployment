package edu.uiowa.team7;

import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class StatusController {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(StatusController.class);

    // Helpers
    private String B64Decode(String encoded) {
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

            if (!Queries.ValidateCredentials(userID, password)) {
                res.setStatus(HttpStatus.NOT_FOUND.value());
                return;
            }

            String device = Security.GetDevice(req);
            res.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieFresh(userID, device));
            res.setStatus(HttpStatus.OK.value());

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
            if (!Queries.ValidateSecurityAnswer(userID, ans)) {
                res.setStatus(HttpStatus.NOT_FOUND.value());
                return "Unauthorized";
            }

            String device = Security.GetDevice(req);
            res.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieFresh(userID, device));
            res.setStatus(HttpStatus.OK.value());
            return "Authorized";
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

        if (!result.IsValid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return "Not Logged In!";
        }

        result.TryRefreshToken(request, response);
        return result.UserID();
    }
}