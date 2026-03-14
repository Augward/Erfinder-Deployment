package edu.uiowa.team7;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Optional;

@RestController
public class StatusController {
    private String B64Decode(String encoded) {
        return new String(Base64.getDecoder().decode(encoded));
    }

    @GetMapping(value = "/api/status", produces = "application/json")
    public String getStatus() {
        return "{\"message\": \"ER Finder Backend is live and connected to MySQL!\"}";
    }

    @GetMapping(value ="/api/gettoken")
    public void GetToken(
            HttpServletRequest req, // what was received
            HttpServletResponse res // what is being sent
    ) {
        try {
            // grabs the userID and password parameters from the GET request
            String userID = B64Decode(req.getParameter("userID"));
            String password = B64Decode(req.getParameter("password"));

            // sends to MySQL server, and leaves if not valid
            if (!Queries.ValidateCredentials(userID, password)) {
                res.setStatus(HttpStatus.NOT_FOUND.value());
                return;
            }

            // using 'device' as a generic term. 'session' is more accurate, probably.
            String device = Security.GetDevice(req);

            // adds the token as a cookie in the response
            res.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieFresh(userID, device));
            res.setStatus(HttpStatus.OK.value());

        } catch (Exception e) {
            // THERE WAS AN ERROR!! OH NO!!
            //  remove cookies, if there were any
            res.getHeaders(HttpHeaders.SET_COOKIE).clear();
            // then tell the client that there was an error:
            res.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }

    @GetMapping("/api/usersecq")
    public String GetSecurityQuestion(
            HttpServletRequest req, // what was received
            HttpServletResponse res // what is being sent
    ) {
        try {
            String userid = B64Decode(req.getParameter("userid"));
            Optional<String> secq = Queries.GetSecurityQuestion(userid);
            if (secq.isEmpty()) {
                res.setStatus(HttpStatus.NOT_FOUND.value());
                return "";
            }
            return secq.get();
        } catch (Exception e) {
            res.setStatus(HttpStatus.NOT_FOUND.value());
            return "";
        }
    }

    @GetMapping("/api/userseca")
    public String GetTokenFromSecq(
            HttpServletRequest req, // what was received
            HttpServletResponse res // what is being sent
    ) {
        String userID = B64Decode(req.getParameter("userid"));
        String ans = B64Decode(req.getParameter("answer"));
        try {

            if (!Queries.ValidateSecurityAnswer(userID, ans)) {
                res.setStatus(HttpStatus.NOT_FOUND.value());
                return "no sql error";
            }

            String device = Security.GetDevice(req);
            res.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieFresh(userID, device));
            res.setStatus(HttpStatus.OK.value());
            return "good";
        } catch (Exception e) {
            res.getHeaders(HttpHeaders.SET_COOKIE).clear();
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return e.getMessage();
        }
    }

    @GetMapping("/api/logout")
    public void Logout(HttpServletResponse res) {
        // creates a cookie that is already expired
        // this is the proper way to 'delete' a cookie
        res.addHeader(HttpHeaders.SET_COOKIE, Security.BuildJWTCookieDelete());
        res.setStatus(HttpStatus.OK.value());
    }


    @GetMapping("/api/username")
    public String GetUsernameInToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // looks at cookie data in the request
        Security.TokenParseResult result = Security.ParseRequestJWT(request);

        // if there is no token / token is expired, it won't be valid
        if (result.IsValid()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return "Not Logged In!";
        }

        // un-expired tokens get refreshed after some time
        result.TryRefreshToken(request, response);

        // this method returns the userID embedded in the token
        return result.UserID();
    }
}
