package com.jobboard.jobportal.security;

import com.jobboard.jobportal.entity.User;
import com.jobboard.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // 단일 role 필드 → 권한 1개 생성 (예: "ROLE_USER")
        String roleName = u.getRole().name(); // enum → "ROLE_USER" 등
        var authorities = Set.of(new SimpleGrantedAuthority(roleName));

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())    // 반드시 BCrypt로 저장되어 있어야 함
                .authorities(authorities)
                .accountLocked(false)
                .disabled(false)
                .build();
    }
}
