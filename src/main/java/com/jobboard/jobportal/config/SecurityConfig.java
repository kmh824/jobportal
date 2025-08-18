package com.jobboard.jobportal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@Profile({"prod", "stage", "test", "local"}) // 기본 로컬도 운영과 '동일 정책'으로 돌립니다.
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS는 WebConfig에서 설정하고, 여기서는 '활성화'만 합니다(하드코딩 X).
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                // 무상태 (세션 사용하지 않음)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 인가 설정
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 인증 없이 접근 가능한 인증 관련 엔드포인트 (POST만 여는 정책이면 아래처럼)
                        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        // (선택) 헬스체크 허용
                        .requestMatchers("/actuator/health").permitAll()
                        // 나머지는 인증 필수
                        .anyRequest().authenticated()
                );

        // JWT 필터는 2단계에서 추가: http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager (로그인 단계에서 사용)
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
