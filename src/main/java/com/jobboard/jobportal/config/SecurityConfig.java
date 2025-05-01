package com.jobboard.jobportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)                // CSRF 보호 끄기
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll())     // 모든 요청 허용
                .formLogin(AbstractHttpConfigurer::disable);           // 기본 로그인 폼 끄기
        return http.build();
    }
}
