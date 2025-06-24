package com.example.security.repos;

import com.example.security.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityDao extends JpaRepository<UserActivity, Long> {
}