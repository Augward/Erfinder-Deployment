package edu.uiowa.team7;

import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Optional;

// User Account API Endpoints
@RestController
public class UserController {

    // Component Logger
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private EmailService emailService;

    // Base64 Decoding Utility
    private String B64Decode(String encoded) {
        if (encoded == null) return "";
        return new String(Base64.getDecoder().decode(encoded.replace(" ", "+")));
    }

    // Profile Data Retrieval
    @GetMapping("/api/myinfo")
    public String GetMyInfo(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (result.Invalid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return "Not logged in";
        }
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
        if (result.Invalid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
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

    // Fetch Linked Insurance
    @GetMapping("/api/getinsurances")
    public String GetInsurances(HttpServletRequest req, HttpServletResponse res) {
        Security.TokenParseResult result = Security.ParseRequestJWT(req);
        if (result.Invalid()) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            return "";
        }
        try {
            return Queries.GetInsurances(result.UserID());
        } catch (Exception e) {
            logger.error("Failed to grab insurances:" + e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return "";
    }

    // Submit Insurance Revisions
    @GetMapping("/api/setinsurance")
    public void SetInsurance(HttpServletRequest req, HttpServletResponse res) {
        Security.TokenParseResult result = Security.ParseRequestJWT(req);
        if (result.Invalid()) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        try {
            boolean fresh = req.getParameter("f").equals("y");
            String blob = B64Decode(req.getParameter("blob"));
            Queries.SetInsurance(result.UserID(), blob, fresh);
        } catch (Exception e) {
            logger.error("Failed to set insurance: " + e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Process Insurance Deletion
    @GetMapping("/api/delinsurance")
    public void DeleteInsurance(HttpServletRequest req, HttpServletResponse res) {
        Security.TokenParseResult result = Security.ParseRequestJWT(req);
        if (result.Invalid()) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        try {
            String blob = B64Decode(req.getParameter("blob"));
            Queries.DeleteInsurance(result.UserID(), blob);
        } catch (Exception e) {
            logger.error("Failed to remove insurance info: " + e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Profile Password Updates
    @GetMapping("/api/updatepassword")
    public void UpdatePassword(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (result.Invalid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
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

    // Account Self-Termination
    @GetMapping("/api/deleteaccount")
    public void DeleteAccount(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (result.Invalid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        try {
            String pass = B64Decode(request.getParameter("password"));

            if (Queries.ValidateCredentials(result.UserID(), pass).isValid) {
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

    // Admin Pending Accounts Array
    @GetMapping("/api/pendingusers")
    public String GetPendingUsers(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (result.Invalid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return "";
        }
        try {
            return Queries.GetPendingUsers();
        } catch (Exception e) {
            logger.error("Failed to fetch pending users", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return "";
        }
    }

    // Admin Approve Account
    @GetMapping("/api/approveuser")
    public void ApproveUser(HttpServletRequest request, HttpServletResponse response) {
        Security.TokenParseResult result = Security.ParseRequestJWT(request);
        if (result.Invalid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        try {
            String target = B64Decode(request.getParameter("target"));
            if (Queries.ApproveUser(target)) {
                response.setStatus(HttpStatus.OK.value());

                // Admin Approval Alert
                Optional<String> targetEmail = Queries.GetUserEmail(target);

                if (targetEmail.isEmpty()) {
                    logger.error("Could not get user email.");
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    return;
                }

                emailService.sendEmail(targetEmail.get(),
                        "ERFinder Account Approved",
                        "Hello " + target + ", your ERFinder account request has been approved by an administrator! You may now log in to the system.");

            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            logger.error("Failed to approve user", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Admin Reject Account
    @GetMapping("/api/rejectuser")
    public ResponseEntity<String> rejectUser(@RequestParam("target") String b64Target) {
        try {
            String targetUserID = new String(Base64.getDecoder().decode(b64Target));

            // Admin Rejection Alert
            Optional<String> targetEmail = Queries.GetUserEmail(targetUserID);

            if (targetEmail.isPresent()) {
                emailService.sendEmail(targetEmail.get(),
                        "ERFinder Account Update",
                        "Your account registration request has been denied by an administrator.");
            }

            Queries.DeleteUser(targetUserID);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database Error");
        }
    }
}