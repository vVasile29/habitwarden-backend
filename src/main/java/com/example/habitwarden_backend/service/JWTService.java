package com.example.habitwarden_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JWTService {
    private final SecretKey key;
    private final JwtParser parser;

    public JWTService() {
        this.key = Keys.hmacShaKeyFor("thisisasupersafekeythatcannotbehacked".getBytes());
        this.parser = Jwts.parserBuilder().setSigningKey(this.key).build();
    }

    public String generate(String userName) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(14, ChronoUnit.DAYS)))
                .signWith(key);

        return builder.compact();
    }

    public String getUserName(String token) {
        try {
            Claims claims = parser.parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validate(UserDetails user, String token) {
        try {
            Claims claims = parser.parseClaimsJws(token).getBody();
            boolean unexpired = claims.getExpiration().after(Date.from(Instant.now()));
            return unexpired && user.getUsername().equals(claims.getSubject());
        } catch (Exception e) {
            return false;
        }
    }
}
