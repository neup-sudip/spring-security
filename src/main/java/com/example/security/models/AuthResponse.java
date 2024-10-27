package com.example.security.models;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthResponse {
    private String authToken;
    private int expiryIn;
}