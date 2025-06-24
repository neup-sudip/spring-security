package com.example.security.utils;


import com.example.security.entity.Customer;
import com.example.security.models.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;

@Service
public class JwtUtils {

    @Value("${jwt.secret.key}")
    private String JWT_SECRET;

    @Value("${jwt.expire.sec}")
    private String JWT_EXPIRE;

    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
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
        calendar.add(Calendar.SECOND, Integer.parseInt(JWT_EXPIRE));
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

        ObjectMapper mapper = new ObjectMapper();
        Customer customer = mapper.convertValue(claims.get("user"), Customer.class);
        return customer.getUsername();
    }

    public AuthResponse getAuthResponse(Customer customer){
        String token = generateToken(Customer.builder().username(customer.getUsername()).build());
        return new AuthResponse(token, Integer.parseInt(JWT_EXPIRE));
    }
}