package com.cokosk.homeserve.interceptor;

import com.cokosk.homeserve.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 角色权限拦截器
 * 根据Token中的角色和登录类型校验接口访问权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("权限拦截: path={}, method={}", path, method);
        
        // 获取Token
        String token = extractToken(request);
        
        // 白名单路径：无需Token校验
        if (isWhiteListPath(path)) {
            return true;
        }
        
        // Token不存在或无效
        if (token == null || !jwtUtil.validateToken(token)) {
            sendErrorResponse(response, 401, "未登录或Token已过期");
            return false;
        }
        
        // 解析Token获取角色和登录类型
        Integer role = jwtUtil.getRoleFromToken(token);
        String loginType = jwtUtil.getLoginTypeFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        if (role == null) {
            sendErrorResponse(response, 401, "Token解析失败");
            return false;
        }
        
        // 将用户信息存入request，供后续使用
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentRole", role);
        request.setAttribute("currentLoginType", loginType);
        
        // ========== 权限校验规则 ==========
        
        // 1. 工人端专属接口（需要 role >= 1 且 loginType = worker）
        if (isWorkerOnlyPath(path)) {
            if (role < 1) {
                sendErrorResponse(response, 403, "该功能仅对工人开放，请先申请成为服务者");
                return false;
            }
            if (!"worker".equals(loginType)) {
                sendErrorResponse(response, 403, "请使用工人端登录访问此功能");
                return false;
            }
        }
        
        // 2. 管理员专属接口（需要 role >= 2）
        if (isAdminOnlyPath(path)) {
            if (role < 2) {
                sendErrorResponse(response, 403, "该功能仅对管理员开放");
                return false;
            }
        }
        
        // 3. 用户端专属接口（loginType = user 时可访问）
        if (isUserOnlyPath(path)) {
            if ("worker".equals(loginType) && role >= 1) {
                // 工人以用户身份登录时可访问用户端接口
                // 但如果 loginType=worker，应限制在工人端接口
                sendErrorResponse(response, 403, "当前为工人端登录，请切换到用户端访问此功能");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // 也支持从参数获取
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }
        return null;
    }
    
    /**
     * 白名单路径：无需Token校验
     */
    private boolean isWhiteListPath(String path) {
        return path.startsWith("/api/user/login")
                || path.startsWith("/api/user/register")
                || path.startsWith("/api/user/send-code")
                || path.startsWith("/api/user/verify-token")
                || path.startsWith("/health")
                || path.startsWith("/api/service")  // 服务列表公开
                || path.startsWith("/api/service-category");  // 服务分类公开
    }
    
    /**
     * 工人端专属接口
     */
    private boolean isWorkerOnlyPath(String path) {
        return path.startsWith("/api/order/grab")
                || path.startsWith("/api/order/grab-pool")
                || path.startsWith("/api/order/worker-");
    }
    
    /**
     * 管理员专属接口
     */
    private boolean isAdminOnlyPath(String path) {
        return path.startsWith("/api/admin");
    }
    
    /**
     * 用户端专属接口
     */
    private boolean isUserOnlyPath(String path) {
        return path.startsWith("/api/order/create")
                || path.startsWith("/api/payment")
                || path.startsWith("/api/review/create");
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
