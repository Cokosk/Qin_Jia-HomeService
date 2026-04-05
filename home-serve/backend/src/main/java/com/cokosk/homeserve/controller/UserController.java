package com.cokosk.homeserve.controller;

import com.cokosk.homeserve.dto.ChangePasswordRequest;
import com.cokosk.homeserve.dto.LoginRequest;
import com.cokosk.homeserve.dto.RegisterRequest;
import com.cokosk.homeserve.dto.UpdatePhoneRequest;
import com.cokosk.homeserve.dto.UpdateUserInfoRequest;
import com.cokosk.homeserve.entity.User;
import com.cokosk.homeserve.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 已升级：参数校验 @Valid + JWT Token认证 + 用户端/工人端登录区分
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    private final UserService userService;
    
    /**
     * 用户登录（支持用户端/工人端区分）
     * POST /api/user/login
     * 
     * Request Body:
     * {
     *   "username": "xxx",
     *   "password": "xxx",
     *   "loginType": "user" 或 "worker"  // 可选，默认 "user"
     * }
     */
    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        String loginType = request.getLoginType();
        if (loginType == null || loginType.isEmpty()) {
            loginType = "user";
        }
        log.info("用户登录: username={}, loginType={}", request.getUsername(), loginType);
        return userService.loginWithType(request.getUsername(), request.getPassword(), loginType);
    }
    
    /**
     * 用户注册（默认注册为普通用户 role=0）
     * POST /api/user/register
     */
    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册: username={}", request.getUsername());
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        
        return userService.register(user);
    }
    
    /**
     * 获取用户信息
     * GET /api/user/info?userId=xxx
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        
        User user = userService.getUserById(userId);
        if (user != null) {
            result.put("code", 200);
            result.put("data", user);
        } else {
            result.put("code", 404);
            result.put("message", "用户不存在");
        }
        
        return result;
    }
    
    /**
     * 验证Token
     * GET /api/user/verify-token?token=xxx
     */
    @GetMapping("/verify-token")
    public Map<String, Object> verifyToken(@RequestParam String token) {
        log.info("验证Token");
        return userService.verifyToken(token);
    }
    
    /**
     * 获取用户完整信息
     * GET /api/user/full-info?userId=xxx
     */
    @GetMapping("/full-info")
    public Map<String, Object> getFullUserInfo(@RequestParam Long userId) {
        log.info("获取用户完整信息: userId={}", userId);
        return userService.getFullUserInfo(userId);
    }
    
    /**
     * 更新用户基本信息
     * POST /api/user/update-info
     */
    @PostMapping("/update-info")
    public Map<String, Object> updateUserInfo(@Valid @RequestBody UpdateUserInfoRequest request) {
        log.info("更新用户信息: userId={}, nickname={}", request.getUserId(), request.getNickname());
        return userService.updateUserInfo(request.getUserId(), request.getNickname(), request.getAvatar());
    }
    
    /**
     * 修改密码
     * POST /api/user/change-password
     */
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码: userId={}", request.getUserId());
        return userService.changePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
    }
    
    /**
     * 发送验证码
     * POST /api/user/send-code
     */
    @PostMapping("/send-code")
    public Map<String, Object> sendVerifyCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        log.info("发送验证码: phone={}", phone);
        return userService.sendVerifyCode(phone);
    }
    
    /**
     * 绑定手机号
     * POST /api/user/update-phone
     */
    @PostMapping("/update-phone")
    public Map<String, Object> updatePhone(@Valid @RequestBody UpdatePhoneRequest request) {
        log.info("绑定手机号: userId={}, phone={}", request.getUserId(), request.getPhone());
        return userService.updatePhone(request.getUserId(), request.getPhone(), request.getVerifyCode());
    }
    
    /**
     * 申请成为服务者（工人）
     * POST /api/user/apply-worker
     */
    @PostMapping("/apply-worker")
    public Map<String, Object> applyWorker(@RequestParam Long userId) {
        log.info("申请成为服务者: userId={}", userId);
        return userService.applyWorker(userId);
    }
}
