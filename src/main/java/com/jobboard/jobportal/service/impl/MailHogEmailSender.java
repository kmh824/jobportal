// src/main/java/com/jobboard/jobportal/service/impl/MailHogEmailSender.java
package com.jobboard.jobportal.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.jobboard.jobportal.service.EmailSender;

@Service
public class MailHogEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(MailHogEmailSender.class);
    private final JavaMailSender mailSender;

    public MailHogEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("no-reply@jobportal.com");
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        try {
            mailSender.send(msg);
            log.info("Mail sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send mail to {}", to, e);
            throw e;
        }
    }
}
