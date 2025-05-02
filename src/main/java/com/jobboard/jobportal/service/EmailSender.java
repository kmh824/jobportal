// src/main/java/com/jobboard/jobportal/service/EmailSender.java
package com.jobboard.jobportal.service;

public interface EmailSender {
    /**
     * 텍스트 이메일 전송
     * @param to      받는 사람 이메일
     * @param subject 메일 제목
     * @param body    메일 본문 (텍스트)
     */
    void send(String to, String subject, String body);
}
