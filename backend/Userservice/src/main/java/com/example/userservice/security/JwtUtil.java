package com.example.userservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username,String role,long id) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .claim("role",role)
                .claim("id",id)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token){
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public String extractRoles(String token) {
        Claims claims = extractAllClaims(token); // your existing claim extraction
        return (String)claims.get("role");
    }

    public Long extractId(String token) {
        Claims claims = extractAllClaims(token);
        return (Long)claims.get("id");
    }

    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        Date exp = extractExpiration(token);
        return exp.before(new Date());
    }


    public boolean isTokenValid(String token,String username) {
        final String subject = extractUsername(token);
        return (subject != null && subject.equals(username) && !isTokenExpired(token));
    }

    public void validateOrThrow(String token) throws JwtException {
        extractAllClaims(token);
        if (isTokenExpired(token)) {
            throw new ExpiredJwtException(null, null, "Token is expired");
        }
    }




}