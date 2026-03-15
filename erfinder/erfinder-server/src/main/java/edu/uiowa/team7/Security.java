package edu.uiowa.team7;

import io.jsonwebtoken.*;
import jakarta.servlet.http.*;
import org.slf4j.*;
import org.springframework.http.*;

import java.security.KeyPair;
import java.time.*;
import java.util.*;

public class Security {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    // Key Generation
    private static KeyPair pair;
    private static KeyPair GetPair() {
        if (pair == null) {
            pair = Jwts.SIG.RS256.keyPair().build();
        }
        return pair;
    }

    // Session Tracking (Locks token to IP Address)
    public static String GetDevice(HttpServletRequest req) {
        String ip = req.getRemoteAddr();
        return ip != null ? ip : "unknown_device";
    }

    // JWT Token Construction
    public static String GenerateJWT(String username, String device) {
        return Jwts.builder()
                .claim("userid", username)
                .claim("device", device)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(Configuration.TokenLife())))
                .signWith(GetPair().getPrivate())
                .compact();
    }

    // HTTP Cookie Builders
    public static String BuildJWTCookieFresh(String username, String device) {
        return ResponseCookie
                .from("token", GenerateJWT(username, device))
                .httpOnly(true)
                .path("/")
                .secure(false) // Can be set True at some point
                .sameSite("Strict")
                .maxAge(Duration.ofMillis(Configuration.TokenLife()))
                .build().toString();
    }

    public static String BuildJWTCookieDelete() {
        return ResponseCookie
                .from("token", "")
                .httpOnly(true)
                .path("/")
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)
                .build().toString();
    }

    // JWT Parsing and Validation Overloads
    public static TokenParseResult ParseRequestJWT(HttpServletRequest request) {
        Optional<String> token = GetToken(request);
        if (token.isEmpty()) {
            return new TokenParseResult();
        }
        String device = GetDevice(request);
        return ParseRequestJWT(token.get(), device);
    }

    public static TokenParseResult ParseRequestJWT(String token, String device) {
        try {
            return new TokenParseResult(token, device);
        } catch (JwtException e) {
            logger.warn("JWT Exception during manual parse: {}", e.getMessage());
            return new TokenParseResult();
        }
    }

    // Internal Token Result Wrapper
    public static class TokenParseResult {
        private final boolean valid;
        private final boolean needsRefresh;
        private final String userid;

        public boolean IsValid() { return !valid; }
        // public boolean NeedsRefresh() { return needsRefresh; }
        public String UserID() { return userid; }

        // Core parsing logic
        public TokenParseResult(String token, String device) {
            String casted;
            boolean refresh;
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(pair.getPublic())
                        .require("device", device)
                        .build().parseSignedClaims(token).getPayload();

                casted = (String) claims.get("userid");
                Date issuedAt = claims.getIssuedAt();
                Instant refreshTime = issuedAt.toInstant().plusMillis(Configuration.TokenNoRefresh());
                Instant now = Instant.now();
                refresh = refreshTime.isBefore(now) && claims.getExpiration().after(Date.from(Instant.now()));
            } catch (JwtException ex) {
                logger.warn("JWT validation failed (Expired or Tampered): {}", ex.getMessage());
                userid = "";
                needsRefresh = false;
                valid = false;
                return;
            }
            userid = casted;
            needsRefresh = refresh;
            valid = true;
        }

        // Empty Constructor
        public TokenParseResult() {
            userid = "";
            needsRefresh = false;
            valid = false;
        }

        // Refresh Handler
        public void TryRefreshToken(HttpServletRequest req, HttpServletResponse res) {
            if (!valid || !needsRefresh) {
                return;
            }
            res.addHeader(HttpHeaders.SET_COOKIE, BuildJWTCookieFresh(userid, GetDevice(req)));
        }
    }

    // Helper Method: Extracts token from request cookies
    private static Optional<String> GetToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}