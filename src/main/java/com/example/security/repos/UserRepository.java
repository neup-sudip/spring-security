package com.example.security.repos;

import com.example.security.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUsername(String username);

    @Query(value = "SELECT * FROM users WHERE user_id <> :id AND username = :username LIMIT 1", nativeQuery = true)
    Customer findByNotIdAndUsername(long id, String username);

    @Query(value = "SELECT * FROM users WHERE username = :username AND password = :password", nativeQuery = true)
    Customer findByUsernameAndPassword(String username, String password);

}
