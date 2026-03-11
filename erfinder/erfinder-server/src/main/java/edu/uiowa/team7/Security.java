package edu.uiowa.team7;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import javax.swing.text.html.Option;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public class Security {
    private static KeyPair pair;
    private static KeyPair GetPair() {
        if (pair == null) {
            pair = Jwts.SIG.RS256.keyPair().build();
        }
        return pair;
    }

    public static String GetDevice(HttpServletRequest req) {
        return req.getRequestedSessionId();
    }

    public static String GenerateJWT(String username, String device) {
        return Jwts.builder()
                .claim("userid",username)
                .claim("device", device)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(Configuration.TokenLife())))
                .signWith(GetPair().getPrivate())
                .compact();
    }

    public static String BuildJWTCookieFresh(String username, String device) {
        return ResponseCookie
                .from("token", GenerateJWT(username, device))
                .httpOnly(true)
                .path("/")
                .secure(true)
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
            return new TokenParseResult();
        }
    }

    public static class TokenParseResult {
        private final boolean valid;
        private final boolean needsRefresh;
        private final String userid;

        public boolean IsValid() { return valid; };
        public boolean NeedsRefresh() { return needsRefresh; }
        public String UserID() { return userid; }

        public TokenParseResult(String token, String device) {

            String casted;
            boolean refresh;
            try {

                Claims claims = Jwts.parser()
                        .verifyWith(pair.getPublic())
                        .require("device", device)
                        .build().parseSignedClaims(token).getPayload();

                casted = (String)claims.get("userid");
                Date issuedAt = claims.getIssuedAt();
                Instant refreshTime = issuedAt.toInstant().plusMillis(Configuration.TokenNoRefresh());
                Instant now = Instant.now();
                refresh = refreshTime.isBefore(now) && claims.getExpiration().after(Date.from(Instant.now()));
            } catch (JwtException ex) {
                userid = "";
                needsRefresh = false;
                valid = false;
                return;
            }
            userid = casted;
            needsRefresh = refresh;
            valid = false;
        }

        public TokenParseResult() {
            userid = "";
            needsRefresh = false;
            valid = false;
        }

        public void TryRefreshToken(HttpServletRequest req, HttpServletResponse res) {
            if (!valid || !needsRefresh) {
                return;
            }
            res.addHeader(HttpHeaders.SET_COOKIE, BuildJWTCookieFresh(userid, GetDevice(req)));
        }
    }

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
