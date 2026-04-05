package com.cokosk.homeserve.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cokosk.homeserve.entity.User;
import com.cokosk.homeserve.exception.BusinessException;
import com.cokosk.homeserve.mapper.UserMapper;
import com.cokosk.homeserve.security.JwtUtil;
import com.cokosk.homeserve.security.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务类
 * 已升级：BCrypt密码加密 + JWT Token认证 + 用户端/工人端登录区分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {
    
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 用户登录（兼容旧接口，默认用户端登录）
     */
    public Map<String, Object> login(String username, String password) {
        return loginWithType(username, password, "user");
    }
    
    /**
     * 用户登录（支持登录类型区分）
     * @param username 用户名
     * @param password 密码
     * @param loginType 登录类型 "user" 或 "worker"
     */
    public Map<String, Object> loginWithType(String username, String password, String loginType) {
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (username == null || username.isEmpty()) {
            result.put("code", 400);
            result.put("message", "用户名不能为空");
            return result;
        }
        if (password == null || password.isEmpty()) {
            result.put("code", 400);
            result.put("message", "密码不能为空");
            return result;
        }
        
        // 默认登录类型为用户端
        if (loginType == null || loginType.isEmpty()) {
            loginType = "user";
        }
        
        // 查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("status", 1);
        
        User user = this.getOne(queryWrapper);
        
        if (user == null) {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        // 使用BCrypt验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        // ========== 登录类型校验（新增） ==========
        if ("user".equals(loginType)) {
            // 用户端登录：允许 role=0（普通用户）或 role=1（服务者以用户身份登录）
            if (user.getRole() != null && user.getRole() == 2) {
                result.put("code", 403);
                result.put("message", "该账号为管理员身份，请使用管理后台登录");
                return result;
            }
        } else if ("worker".equals(loginType)) {
            // 工人端登录：必须 role >= 1（服务者或管理员）
            if (user.getRole() == null || user.getRole() < 1) {
                result.put("code", 403);
                result.put("message", "该账号未开通工人身份，请先申请成为服务者");
                return result;
            }
        } else {
            result.put("code", 400);
            result.put("message", "登录类型无效，请选择用户登录或工人登录");
            return result;
        }
        
        // 使用JWT生成Token（包含登录类型）
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), loginType);
        
        // 缓存JWT Token到Redis（24小时）
        String cacheKey = "user:token:" + username + ":" + loginType;
        redisTemplate.opsForValue().set(cacheKey, token, Duration.ofHours(24));
        
        // 缓存用户详情
        String userKey = "user:info:" + user.getId();
        String nickname = user.getNickname() != null ? user.getNickname() : "";
        redisTemplate.opsForHash().putAll(userKey, Map.of(
            "id", String.valueOf(user.getId()),
            "username", user.getUsername(),
            "nickname", nickname,
            "role", String.valueOf(user.getRole()),
            "loginType", loginType
        ));
        redisTemplate.expire(userKey, Duration.ofHours(24));
        
        result.put("code", 200);
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("role", user.getRole());
        result.put("loginType", loginType);
        result.put("message", "登录成功");
        
        log.info("用户登录成功: username={}, userId={}, loginType={}", username, user.getId(), loginType);
        
        return result;
    }
    
    /**
     * 用户注册
     * 使用BCrypt加密密码
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            result.put("code", 400);
            result.put("message", "用户名不能为空");
            return result;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            result.put("code", 400);
            result.put("message", "密码不能为空");
            return result;
        }
        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            result.put("code", 400);
            result.put("message", "用户名长度应为3-20个字符");
            return result;
        }
        if (user.getPassword().length() < 6) {
            result.put("code", 400);
            result.put("message", "密码长度不能少于6个字符");
            return result;
        }
        
        // 检查用户名是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (this.count(queryWrapper) > 0) {
            result.put("code", 400);
            result.put("message", "用户名已存在");
            return result;
        }
        
        // 检查手机号是否已被使用（如果提供了手机号）
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            QueryWrapper<User> phoneQuery = new QueryWrapper<>();
            phoneQuery.eq("phone", user.getPhone());
            if (this.count(phoneQuery) > 0) {
                result.put("code", 400);
                result.put("message", "该手机号已被注册");
                return result;
            }
        }
        
        // 使用BCrypt加密密码
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
        // 设置默认值
        user.setRole(0); // 普通用户
        user.setStatus(1); // 正常
        user.setCreditScore(100); // 默认信用分
        
        boolean saved = this.save(user);
        
        if (saved) {
            result.put("code", 200);
            result.put("message", "注册成功");
            result.put("userId", user.getId());
        } else {
            result.put("code", 500);
            result.put("message", "注册失败");
        }
        
        return result;
    }
    
    /**
     * 根据ID获取用户信息（优先从Redis缓存）
     */
    public User getUserById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        
        String userKey = "user:info:" + userId;
        
        // 尝试从Redis获取
        Map<Object, Object> cached = redisTemplate.opsForHash().entries(userKey);
        if (cached != null && !cached.isEmpty() && cached.get("id") != null) {
            User user = new User();
            try {
                user.setId(Long.parseLong((String) cached.get("id")));
                user.setUsername((String) cached.get("username"));
                user.setNickname((String) cached.get("nickname"));
                user.setRole(Integer.parseInt((String) cached.get("role")));
                return user;
            } catch (NumberFormatException e) {
                log.warn("解析缓存用户信息失败: userId={}", userId);
                // 缓存解析失败，清除缓存并从数据库获取
                redisTemplate.delete(userKey);
            }
        }
        
        // 从数据库获取并缓存
        User user = this.getById(userId);
        if (user != null) {
            String nickname = user.getNickname() != null ? user.getNickname() : "";
            redisTemplate.opsForHash().putAll(userKey, Map.of(
                "id", String.valueOf(user.getId()),
                "username", user.getUsername(),
                "nickname", nickname,
                "role", String.valueOf(user.getRole())
            ));
            redisTemplate.expire(userKey, Duration.ofHours(24));
        }
        
        return user;
    }
    
    /**
     * 清除用户缓存
     */
    public void clearCache(Long userId) {
        if (userId != null) {
            redisTemplate.delete("user:info:" + userId);
        }
    }
    
    /**
     * 更新用户基本信息（昵称、头像）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateUserInfo(Long userId, String nickname, String avatar) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        
        User user = this.getById(userId);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }
        
        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar);
        }
        
        boolean updated = this.updateById(user);
        
        if (updated) {
            clearCache(userId);
            
            result.put("code", 200);
            result.put("message", "信息更新成功");
            result.put("data", user);
            
            log.info("用户信息更新: userId={}, nickname={}", userId, nickname);
        } else {
            result.put("code", 500);
            result.put("message", "更新失败");
        }
        
        return result;
    }
    
    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            result.put("code", 400);
            result.put("message", "原密码不能为空");
            return result;
        }
        if (newPassword == null || newPassword.isEmpty()) {
            result.put("code", 400);
            result.put("message", "新密码不能为空");
            return result;
        }
        if (newPassword.length() < 6) {
            result.put("code", 400);
            result.put("message", "新密码长度不能少于6个字符");
            return result;
        }
        
        User user = this.getById(userId);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }
        
        // 使用BCrypt验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            result.put("code", 400);
            result.put("message", "原密码错误");
            return result;
        }
        
        // 验证新密码不能与旧密码相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            result.put("code", 400);
            result.put("message", "新密码不能与原密码相同");
            return result;
        }
        
        // 使用BCrypt加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean updated = this.updateById(user);
        
        if (updated) {
            // 清除用户Token缓存，强制重新登录
            redisTemplate.delete("user:token:" + user.getUsername() + ":user");
            redisTemplate.delete("user:token:" + user.getUsername() + ":worker");
            clearCache(userId);
            
            result.put("code", 200);
            result.put("message", "密码修改成功，请重新登录");
            
            log.info("用户修改密码: userId={}", userId);
        } else {
            result.put("code", 500);
            result.put("message", "密码修改失败");
        }
        
        return result;
    }
    
    /**
     * 绑定/修改手机号
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePhone(Long userId, String phone, String verifyCode) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        
        User user = this.getById(userId);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }
        
        // 验证手机号格式
        if (phone == null || phone.length() != 11 || !phone.matches("^1[3-9]\\d{9}$")) {
            result.put("code", 400);
            result.put("message", "手机号格式不正确");
            return result;
        }
        
        // 检查手机号是否已被使用
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        queryWrapper.ne("id", userId);
        if (this.count(queryWrapper) > 0) {
            result.put("code", 400);
            result.put("message", "该手机号已被其他用户绑定");
            return result;
        }
        
        // Mock验证码校验
        String cachedCode = redisTemplate.opsForValue().get("sms:code:" + phone);
        if (cachedCode == null || !cachedCode.equals(verifyCode)) {
            if (!"123456".equals(verifyCode)) {
                result.put("code", 400);
                result.put("message", "验证码错误或已过期");
                return result;
            }
        }
        
        user.setPhone(phone);
        boolean updated = this.updateById(user);
        
        if (updated) {
            clearCache(userId);
            redisTemplate.delete("sms:code:" + phone);
            
            result.put("code", 200);
            result.put("message", "手机号绑定成功");
            
            log.info("用户绑定手机号: userId={}, phone={}", userId, phone);
        } else {
            result.put("code", 500);
            result.put("message", "绑定失败");
        }
        
        return result;
    }
    
    /**
     * 发送验证码（Mock）
     */
    public Map<String, Object> sendVerifyCode(String phone) {
        Map<String, Object> result = new HashMap<>();
        
        if (phone == null || phone.length() != 11 || !phone.matches("^1[3-9]\\d{9}$")) {
            result.put("code", 400);
            result.put("message", "手机号格式不正确");
            return result;
        }
        
        String verifyCode = "123456";
        redisTemplate.opsForValue().set("sms:code:" + phone, verifyCode, Duration.ofMinutes(5));
        
        result.put("code", 200);
        result.put("message", "验证码已发送");
        result.put("verifyCode", verifyCode);
        
        log.info("发送验证码: phone={}, code={}", phone, verifyCode);
        
        return result;
    }
    
    /**
     * 申请成为服务者
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> applyWorker(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        
        User user = this.getById(userId);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }
        
        if (user.getRole() >= 1) {
            result.put("code", 400);
            result.put("message", "您已是服务者");
            return result;
        }
        
        if (user.getCreditScore() != null && user.getCreditScore() < 80) {
            result.put("code", 400);
            result.put("message", "信用分不足，无法申请成为服务者");
            return result;
        }
        
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            result.put("code", 400);
            result.put("message", "请先绑定手机号");
            return result;
        }
        
        user.setRole(1);
        boolean updated = this.updateById(user);
        
        if (updated) {
            clearCache(userId);
            
            result.put("code", 200);
            result.put("message", "申请成功，您已成为服务者");
            
            log.info("用户成为服务者: userId={}", userId);
        } else {
            result.put("code", 500);
            result.put("message", "申请失败");
        }
        
        return result;
    }
    
    /**
     * 获取用户完整信息
     */
    public Map<String, Object> getFullUserInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        
        User user = this.getById(userId);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        userInfo.put("status", user.getStatus());
        userInfo.put("creditScore", user.getCreditScore());
        userInfo.put("createTime", user.getCreateTime());
        userInfo.put("updateTime", user.getUpdateTime());
        
        result.put("code", 200);
        result.put("data", userInfo);
        
        return result;
    }
    
    /**
     * 验证Token并获取用户信息
     */
    public Map<String, Object> verifyToken(String token) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || token.isEmpty()) {
            result.put("code", 401);
            result.put("message", "Token不能为空");
            return result;
        }
        
        if (!jwtUtil.validateToken(token)) {
            result.put("code", 401);
            result.put("message", "Token无效或已过期");
            return result;
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        String loginType = jwtUtil.getLoginTypeFromToken(token);
        
        if (userId == null || username == null || role == null) {
            result.put("code", 401);
            result.put("message", "Token解析失败");
            return result;
        }
        
        result.put("code", 200);
        result.put("userId", userId);
        result.put("username", username);
        result.put("role", role);
        result.put("loginType", loginType);
        result.put("message", "Token有效");
        
        return result;
    }
}
