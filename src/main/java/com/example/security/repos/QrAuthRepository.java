package com.example.security.repos;

import com.example.security.entity.QrAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrAuthRepository extends JpaRepository<QrAuth, String> {
}