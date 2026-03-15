package edu.uiowa.team7;

import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
public class UserController {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // Helpers
    private String B64Decode(String encoded) {
        if (encoded == null) return "";
        return new String(Base64.getDecoder().decode(encoded.replace(" ", "+")));
    }

    // Profile Data Retrieval
    @GetMapping("/api/myinfo")
    public String GetMyInfo(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (!result.IsValid()) { response.setStatus(HttpStatus.UNAUTHORIZED.value()); return "Not logged in"; }
        try {
            String[] info = Queries.GetUserInfo(result.UserID());
            if (info != null) {
                return String.join(",", info);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve user info", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return "Error loading data";
    }

    // Profile Data Updates
    @GetMapping("/api/updateinfo")
    public void UpdateInfo(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (!result.IsValid()) { response.setStatus(HttpStatus.UNAUTHORIZED.value()); return; }
        try {
            Queries.UpdateUserInfo(
                    result.UserID(),
                    B64Decode(request.getParameter("firstn")), B64Decode(request.getParameter("lastn")),
                    B64Decode(request.getParameter("legaln")), B64Decode(request.getParameter("dln")),
                    B64Decode(request.getParameter("ssn")), B64Decode(request.getParameter("email")),
                    B64Decode(request.getParameter("phone")), B64Decode(request.getParameter("addr")),
                    B64Decode(request.getParameter("zip")), B64Decode(request.getParameter("dob")),
                    B64Decode(request.getParameter("gender")), B64Decode(request.getParameter("contact"))
            );
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            logger.error("Failed to update user info", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Security Updates
    @GetMapping("/api/updatepassword")
    public void UpdatePassword(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (!result.IsValid()) { response.setStatus(HttpStatus.UNAUTHORIZED.value()); return; }
        try {
            String newPass = B64Decode(request.getParameter("newpass"));
            Queries.UpdatePassword(result.UserID(), newPass);

            response.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieDelete());
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            logger.error("Failed to update password", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Account Management
    @GetMapping("/api/deleteaccount")
    public void DeleteAccount(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (!result.IsValid()) { response.setStatus(HttpStatus.UNAUTHORIZED.value()); return; }
        try {
            String pass = B64Decode(request.getParameter("password"));

            if (Queries.ValidateCredentials(result.UserID(), pass)) {
                Queries.DeleteUser(result.UserID());
                response.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieDelete());
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.FORBIDDEN.value());
            }
        } catch (Exception e) {
            logger.error("Failed to delete account", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Admin Capabilities
    @GetMapping("/api/pendingusers")
    public String GetPendingUsers(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (!result.IsValid()) { response.setStatus(HttpStatus.UNAUTHORIZED.value()); return ""; }
        try {
            return Queries.GetPendingUsers();
        } catch (Exception e) {
            logger.error("Failed to fetch pending users", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return "";
        }
    }

    @GetMapping("/api/approveuser")
    public void ApproveUser(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (!result.IsValid()) { response.setStatus(HttpStatus.UNAUTHORIZED.value()); return; }
        try {
            String target = B64Decode(request.getParameter("target"));
            if (Queries.ApproveUser(target)) {
                response.setStatus(HttpStatus.OK.value());
            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            logger.error("Failed to approve user", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}