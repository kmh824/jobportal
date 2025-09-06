package com.jobboard.jobportal.service;

import com.jobboard.jobportal.entity.User;

public interface RefreshTokenService {
    /** 로그인 성공 시 DB에 refresh 기록 저장 */
    void saveLoginToken(User user, String rawRefresh, String userAgent, String ip);

    /** 재발급(회전): 기존 refresh를 무효화하고 새 refresh 반환 */
    String rotate(String rawRefresh, User user, String userAgent, String ip);

    /** 로그아웃: refresh 무효화 */
    void revoke(String rawRefresh);
}
