package com.jobboard.jobportal.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.jobboard.jobportal.config.VerificationProperties;
import com.jobboard.jobportal.entity.EmailVerification;
import com.jobboard.jobportal.exception.InvalidVerificationCodeException;
import com.jobboard.jobportal.exception.VerificationBlockedException;
import com.jobboard.jobportal.exception.VerificationExpiredException;
import com.jobboard.jobportal.repository.EmailVerificationRepository;
import com.jobboard.jobportal.service.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailVerificationServiceImplTest {

    @Mock
    EmailVerificationRepository repository;
    @Mock
    EmailSender emailSender;
    @Mock
    VerificationProperties props;
    @InjectMocks
    EmailVerificationServiceImpl service;

    @BeforeEach
    void setup() {
        when(props.getCodeLength()).thenReturn(6);
        when(props.getExpireMinutes()).thenReturn(5L);
        when(props.getBlockHours()).thenReturn(24L);
        when(props.getMaxAttempts()).thenReturn(3L);
    }

    @Test
    void requestVerificationCode_savesEntityAndSendsEmail() {
        String email = "test@example.com";
        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        service.requestVerificationCode(email);

        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(repository).save(captor.capture());
        EmailVerification ev = captor.getValue();
        assertEquals(email, ev.getEmail());
        assertNotNull(ev.getCode());
        assertEquals(0, ev.getAttemptCount());
        assertNull(ev.getBlockedUntil());
        verify(emailSender).send(eq(email), anyString(), contains(ev.getCode()));
    }

    @Test
    void verifyCode_successDeletesEntity() {
        String email = "test@example.com", code = "123456";
        EmailVerification ev = EmailVerification.builder()
                .email(email).code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(1))
                .attemptCount(0).blockedUntil(null).build();
        when(repository.findByEmail(email)).thenReturn(Optional.of(ev));

        service.verifyCode(email, code);

        verify(repository).deleteByEmail(email);
    }

    @Test
    void verifyCode_throwsExpiredException() {
        String email = "test@example.com", code = "123456";
        EmailVerification ev = EmailVerification.builder()
                .email(email).code(code)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .attemptCount(0).blockedUntil(null).build();
        when(repository.findByEmail(email)).thenReturn(Optional.of(ev));

        assertThrows(VerificationExpiredException.class, () -> service.verifyCode(email, code));
        verify(repository).deleteByEmail(email);
    }

    @Test
    void verifyCode_throwsBlockedExceptionAfterMaxAttempts() {
        String email = "test@example.com", wrong = "000000";
        EmailVerification ev = EmailVerification.builder()
                .email(email).code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(1))
                .attemptCount(2).blockedUntil(null).build();
        when(repository.findByEmail(email)).thenReturn(Optional.of(ev));

        assertThrows(VerificationBlockedException.class, () -> service.verifyCode(email, wrong));
        verify(repository).save(argThat(updated ->
                updated.getBlockedUntil() != null
        ));
    }
}
