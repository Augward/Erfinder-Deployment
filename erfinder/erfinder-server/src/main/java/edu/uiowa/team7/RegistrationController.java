package edu.uiowa.team7;

import org.slf4j.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class RegistrationController {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    // Pre-Registration Validation Endpoint
    @PostMapping("/register/check")
    public Map<String, Boolean> checkExisting(@RequestBody Map<String, String> data) {
        Map<String, Boolean> result = new HashMap<>();
        try {
            result.put("usernameExists", Queries.FieldExists("userid", data.get("username")));
            result.put("emailExists", Queries.FieldExists("email", data.get("email")));
            result.put("ssnExists", Queries.FieldExists("ssn", data.get("ssn")));
            result.put("licenseExists", Queries.FieldExists("dln", data.get("driversLicense")));
        } catch (Exception e) {
            logger.error("Database error during registration existence check", e);
            result.put("usernameExists", false);
            result.put("emailExists", false);
            result.put("ssnExists", false);
            result.put("licenseExists", false);
        }
        return result;
    }

    // Registration Submission Endpoint
    @PostMapping("/register/create")
    @ResponseBody
    public Map<String, String> createRegistration(@RequestBody Map<String, String> data) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean inserted = Queries.insertReg(data);
            response.put("status", inserted ? "success" : "failed");
        } catch (Exception e) {
            logger.error("Failed to insert new registration request", e);
            response.put("status", "error");
            response.put("message", "A server error occurred during registration.");
        }
        return response;
    }
}