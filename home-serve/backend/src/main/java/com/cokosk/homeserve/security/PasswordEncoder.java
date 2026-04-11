package com.cokosk.homeserve.security;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 * 使用BCrypt算法进行密码加密和验证
 */
@Component
public class PasswordEncoder {
    
    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
    
    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
