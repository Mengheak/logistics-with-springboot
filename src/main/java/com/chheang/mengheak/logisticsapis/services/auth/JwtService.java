package com.chheang.mengheak.logisticsapis.services.auth;

import com.chheang.mengheak.logisticsapis.config.ApplicationConfig;
import com.chheang.mengheak.logisticsapis.models.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final int accessMinutes;

    public JwtService(ApplicationConfig applicationConfig) {
        ApplicationConfig.Jwt jwt = applicationConfig.getSecurity().getJwt();
        this.key = Keys.hmacShaKeyFor(jwt.getSecret().getBytes(StandardCharsets.UTF_8));
        this.issuer = jwt.getIssuer();
        this.accessMinutes = jwt.getAccessTokenMinutes();
    }

    public String generateAccessToken(AuthUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessMinutes, ChronoUnit.MINUTES);

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("uid", user.getId().toString())
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    public long getAccessTokenValiditySeconds() {
        return accessMinutes * 60L;
    }

    /** Signature and issuer are already verified by {@link #parse}; this adds the subject check. */
    public boolean validateToken(String token, UserDetails userDetails) {
        return !extractExpiration(token).before(new Date())
                && extractUsername(token).equals(userDetails.getUsername());
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        return claimResolver.apply(parse(token).getPayload());
    }
}
