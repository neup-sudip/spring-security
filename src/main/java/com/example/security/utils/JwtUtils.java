package com.example.security.utils;


import com.example.security.entity.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Service
public class JwtUtils {

    @Value("${jwt.secret.key}")
    private String JWT_SECRET;

    @Value("${jwt.expire.date}")
    private String JWT_EXPIRE;

    public String extractToken(HttpServletRequest request, String key) {
        try {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(key)) {
                    return cookie.getValue();
                }
            }
        } catch (NullPointerException ex) {
            return "";
        }
        return "";
    }

    public boolean validateToken(String token) {
        return token != null && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        if (Boolean.TRUE.equals(decodeToken(token).b)) {
            return decodeToken(token).a.getExpiration().before(new Date(System.currentTimeMillis()));
        }
        return true;
    }

    public String generateToken(Customer user) {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(JWT_EXPIRE));
        Date updatedDate = calendar.getTime();

        return Jwts.builder()
                .setSubject("Token")
                .setIssuedAt(currentDate)
                .setExpiration(updatedDate)
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET.getBytes())
                .claim("user", user)
                .compact();
    }

    public Pair<Claims, Boolean> decodeToken(String token) {
        try {
            Key key = new SecretKeySpec(JWT_SECRET.getBytes(), SignatureAlgorithm.HS512.getJcaName());
            return new Pair<>(Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody(), true);
        } catch (Exception ex) {
            return new Pair<>(null, false);
        }
    }

    public String getUsername(String token) {
        Key key = new SecretKeySpec(JWT_SECRET.getBytes(), SignatureAlgorithm.HS512.getJcaName());
        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();

        Map<String, Object> userMap = claims.get("user", Map.class);
        return (String) userMap.get("username");
    }
}