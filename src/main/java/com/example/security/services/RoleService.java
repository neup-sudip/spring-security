package com.example.security.services;

import com.example.security.entity.Role;
import com.example.security.repos.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    @Cacheable(value = "roleCache", key = "#roleId", unless="#result == null")
    public Optional<Role> getRoleById(long roleId) {
        log.info("From DB");
        return roleRepository.findById(roleId);
    }

}
