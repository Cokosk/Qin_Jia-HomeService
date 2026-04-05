package com.cokosk.homeserve.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cokosk.homeserve.entity.User;
import com.cokosk.homeserve.mapper.UserMapper;
import com.cokosk.homeserve.security.JwtUtil;
import com.cokosk.homeserve.security.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private HashOperations<String, Object, Object> hashOps;
    @Mock private UserMapper userMapper;

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        userService = spy(new UserService(redisTemplate, jwtUtil, passwordEncoder));
        
        Field baseMapperField = UserService.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(userService, userMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setNickname("Test User");
        testUser.setRole(0);
        testUser.setStatus(1);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOps);
        
        // 直接mock ServiceImpl的方法
        doReturn(testUser).when(userService).getOne(any(QueryWrapper.class));
        doReturn(null).when(valueOps).get(anyString());
    }

    @Test
    void testLogin_Success() {
        when(passwordEncoder.matches("password", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser", 0)).thenReturn("token123");

        Map<String, Object> result = userService.login("testuser", "password");

        assertEquals(200, result.get("code"));
        assertEquals("token123", result.get("token"));
        assertEquals(1L, result.get("userId"));
    }

    @Test
    void testLogin_WrongPassword() {
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        Map<String, Object> result = userService.login("testuser", "wrongpassword");

        assertEquals(401, result.get("code"));
        assertNull(result.get("token"));
    }

    @Test
    void testLogin_UserNotFound() {
        doReturn(null).when(userService).getOne(any(QueryWrapper.class));

        Map<String, Object> result = userService.login("nonexistent", "password");

        assertEquals(401, result.get("code"));
    }

    @Test
    void testRegister_Success() {
        doReturn(0L).when(userService).count(any(QueryWrapper.class));
        doReturn(true).when(userService).save(any(User.class));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");

        Map<String, Object> result = userService.register(newUser);

        assertEquals(200, result.get("code"));
    }

    @Test
    void testRegister_DuplicateUsername() {
        doReturn(1L).when(userService).count(any(QueryWrapper.class));

        User newUser = new User();
        newUser.setUsername("existinguser");
        newUser.setPassword("password");

        Map<String, Object> result = userService.register(newUser);

        assertEquals(400, result.get("code"));
    }

    @Test
    void testGetUserById_Cache() {
        Map<Object, Object> cache = new HashMap<>();
        cache.put("id", "1");
        cache.put("username", "testuser");
        cache.put("role", "0");
        when(hashOps.entries(eq("user:info:1"))).thenReturn(cache);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetUserById_Database() {
        when(hashOps.entries(eq("user:info:1"))).thenReturn(new HashMap<>());
        doReturn(testUser).when(userService).getById(1L);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testClearCache() {
        userService.clearCache(1L);
        verify(redisTemplate).delete("user:info:1");
    }

    @Test
    void testVerifyToken_Valid() {
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("token")).thenReturn(1L);
        when(jwtUtil.getUsernameFromToken("token")).thenReturn("testuser");
        when(jwtUtil.getRoleFromToken("token")).thenReturn(0);

        Map<String, Object> result = userService.verifyToken("token");

        assertEquals(200, result.get("code"));
    }

    @Test
    void testVerifyToken_Invalid() {
        when(jwtUtil.validateToken("invalid")).thenReturn(false);

        Map<String, Object> result = userService.verifyToken("invalid");

        assertEquals(401, result.get("code"));
    }
}
