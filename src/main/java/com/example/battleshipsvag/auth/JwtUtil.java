package com.example.battleshipsvag.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
public class JwtUtil {

    private static final String SECRET = "AverageLeagueOfLegendsEnjoyerAverageLeagueOfLegendsEnjoyerAverageLeagueOfLegendsEnjoyerAverageLeagueOfLegendsEnjoyer";
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days in milliseconds

    public static String generateToken(String playerName) {
        return Jwts.builder()
                .setSubject(playerName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public static String getPlayerNameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}