package com.example.security.repos;

import com.example.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface RoleRepository extends JpaRepository<Role, Long>{

    @Query(value = "SELECT * FROM roles WHERE role_id <> :id AND name = :name LIMIT 1", nativeQuery = true)
    Optional<Role> findByNotIdAndName(long id, String name);
}
