package com.example.security.services;

import com.example.security.entity.Customer;
import com.example.security.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userServices;
    private final RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = userServices.login(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new User(
                customer.getUsername(),
                customer.getPassword(),
                getAuthorities(customer.getRole())
        );
    }

    private Set<GrantedAuthority> getAuthorities(int role) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        Optional<Role> optRole = roleService.getRoleById(role);

        if (optRole.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("GET_SINGLE_USER"));
        } else {
            String roleAuths = optRole.get().getAuthorities();
            authorities.addAll(Arrays.stream(roleAuths.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        }

        return authorities;
    }
}