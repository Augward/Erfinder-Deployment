package edu.uiowa.team7;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    @GetMapping(value = "/api/status", produces = "application/json")
    public String getStatus() {
        return "{\"message\": \"ER Finder Backend is live and connected to MySQL!\"}";
    }
}
