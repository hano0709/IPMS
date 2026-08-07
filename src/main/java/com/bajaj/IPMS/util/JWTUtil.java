package com.bajaj.IPMS.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JWTUtil {

    private static final SecretKey key = Keys.hmacShaKeyFor("supersecretkeysupersecretkeysupersecretkey".getBytes());
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    public static String generateToken(String email){
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public static String extractEmail(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
