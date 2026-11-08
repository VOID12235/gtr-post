package com.btr.platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTtl;

    public JwtService(@Value("${btr.jwt.secret}") String secret,
                      @Value("${btr.jwt.access-ttl:PT2H}") Duration accessTtl) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtl = accessTtl;
    }

    public String generate(UserPrincipal principal) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(principal.userId()))
                .claim("tenantId", principal.tenantId())
                .claim("account", principal.account())
                .claim("nickname", principal.nickname())
                .claim("role", principal.role())
                .claim("platformRole", principal.platformRole())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTtl.toMillis()))
                .signWith(key)
                .compact();
    }

    public Optional<UserPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = toLong(claims.getSubject());
            if (userId == null) {
                return Optional.empty();
            }
            return Optional.of(new UserPrincipal(
                    userId,
                    toLong(claims.get("tenantId")),
                    claims.get("account", String.class),
                    claims.get("nickname", String.class),
                    claims.get("role", String.class),
                    claims.get("platformRole", String.class)
            ));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
