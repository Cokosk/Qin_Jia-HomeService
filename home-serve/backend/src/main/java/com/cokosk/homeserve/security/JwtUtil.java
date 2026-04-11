package com.cokosk.homeserve.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 提供Token的生成、解析、验证功能
 * 支持用户端/工人端登录类型区分
 */
@Slf4j
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:home-serve-secret-key-2024-default-secret-key-for-jwt-token}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private Long expiration; // 默认24小时
    
    private Key key;
    
    @PostConstruct
    public void init() {
        // 确保密钥长度足够（至少256位用于HS256）
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * 生成JWT Token（包含登录类型）
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @param loginType 登录类型 "user" 或 "worker"
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, Integer role, String loginType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("loginType", loginType != null ? loginType : "user");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 生成JWT Token（兼容旧接口，默认loginType=user）
     */
    public String generateToken(Long userId, String username, Integer role) {
        return generateToken(userId, username, role, "user");
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
            return userId != null ? Long.parseLong(userId.toString()) : null;
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从Token中获取角色
     */
    public Integer getRoleFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object role = claims.get("role");
            if (role instanceof Integer) {
                return (Integer) role;
            }
            return role != null ? Integer.parseInt(role.toString()) : null;
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从Token中获取登录类型
     */
    public String getLoginTypeFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object loginType = claims.get("loginType");
            return loginType != null ? loginType.toString() : "user";
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token格式错误: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Token签名无效: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token为空: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 解析Token获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
