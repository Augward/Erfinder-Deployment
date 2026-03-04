package edu.uiowa.team7;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    @GetMapping(value = "/api/status", produces = "application/json")
    public String getStatus() {
        return "{\"message\": \"ER Finder Backend is live and connected to MySQL!\"}";
    }

    @GetMapping(value ="/api/validate", produces = "application/json")
    public String Validate(@RequestParam(name = "userID") String userID, @RequestParam(name = "password") String password) {
        try {
            if (Queries.ValidateCredentials(userID, password)) {
                return "{\"message\": \"Valid Credentials!\"}";
            }
        } catch (Exception e) {
            return "{\"message\": \"Server Error\"}";
        }
        return "{\"message\": \"Invalid Credentials!\"}";
    }
}
