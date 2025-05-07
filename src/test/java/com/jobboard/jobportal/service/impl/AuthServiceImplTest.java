package com.jobboard.jobportal.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.jobboard.jobportal.dto.SignupRequest;
import com.jobboard.jobportal.dto.SignupResponse;
import com.jobboard.jobportal.entity.User;
import com.jobboard.jobportal.exception.EmailAlreadyExistsException;
import com.jobboard.jobportal.repository.UserRepository;
import com.jobboard.jobportal.service.EmailVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EmailVerificationService verificationService;
    @InjectMocks
    AuthServiceImpl authService;

    @Test
    void register_success() {
        SignupRequest req = new SignupRequest("user@example.com", "123456", "Abcd1234!");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        doNothing().when(verificationService).verifyCode(req.getEmail(), req.getCode());
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashed");
        User savedUser = User.builder()
                .id(1L)
                .email(req.getEmail())
                .password("hashed")
                .role(User.Role.ROLE_USER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        SignupResponse res = authService.register(req);

        assertEquals(1L, res.getId());
        assertEquals(req.getEmail(), res.getEmail());
        assertEquals("ROLE_USER", res.getRole());
        verify(verificationService).verifyCode(req.getEmail(), req.getCode());
        verify(userRepository).save(any());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        SignupRequest req = new SignupRequest("user@example.com", "123456", "Abcd1234!");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(req));
    }
}
