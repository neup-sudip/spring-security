package com.example.security.entity;

import com.example.security.utils.CryptoConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 16, unique = true)
    private String username;

    @JsonIgnore
    @Column(name = "password")
    @Convert(converter = CryptoConverter.class)
    private String password;

    @Column(name = "role")
    private int role;
}
