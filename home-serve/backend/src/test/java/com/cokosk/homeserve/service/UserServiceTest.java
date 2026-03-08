package com.cokosk.homeserve.service;

import com.cokosk.homeserve.entity.User;
import com.cokosk.homeserve.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private HashOperations<String, Object, Object> hashOps;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setNickname("Test User");
        testUser.setRole(0);
        testUser.setStatus(1);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void testLogin_Success_WithCache() {
        // Given
        String username = "testuser";
        String password = "password";
        String cachedToken = "cached_token_123";

        when(valueOps.get(eq("user:token:testuser"))).thenReturn(cachedToken);

        // When
        Map<String, Object> result = userService.login(username, password);

        // Then
        assertEquals(200, result.get("code"));
        assertEquals(cachedToken, result.get("token"));
        assertEquals("登录成功(缓存)", result.get("message"));
        verify(valueOps, times(1)).get(eq("user:token:testuser"));
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    void testLogin_Success_FromDatabase() {
        // Given
        String username = "testuser";
        String password = "password";

        when(valueOps.get(eq("user:token:testuser"))).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(testUser);

        // When
        Map<String, Object> result = userService.login(username, password);

        // Then
        assertEquals(200, result.get("code"));
        assertNotNull(result.get("token"));
        assertEquals(1L, result.get("userId"));
        assertEquals(0, result.get("role"));
        assertEquals("登录成功", result.get("message"));

        // Verify Redis interactions
        verify(valueOps, times(1)).get(eq("user:token:testuser"));
        verify(valueOps, times(1)).set(anyString(), anyString(), any());
        verify(hashOps, times(1)).putAll(anyString(), any(Map.class));
        verify(userMapper, times(1)).selectOne(any());
    }

    @Test
    void testLogin_Failure_WrongCredentials() {
        // Given
        String username = "testuser";
        String password = "wrongpassword";

        when(valueOps.get(eq("user:token:testuser"))).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(null);

        // When
        Map<String, Object> result = userService.login(username, password);

        // Then
        assertEquals(401, result.get("code"));
        assertEquals("用户名或密码错误", result.get("message"));
        assertNull(result.get("token"));

        verify(valueOps, times(1)).get(eq("user:token:testuser"));
        verify(userMapper, times(1)).selectOne(any());
    }

    @Test
    void testRegister_Success() {
        // Given
        when(userMapper.selectCount(any())).thenReturn(0L); // 用户名不存在
        when(userMapper.insert(any())).thenReturn(1); // 插入成功

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");
        newUser.setNickname("New User");

        // When
        Map<String, Object> result = userService.register(newUser);

        // Then
        assertEquals(200, result.get("code"));
        assertEquals("注册成功", result.get("message"));
        assertNotNull(result.get("userId"));

        verify(userMapper, times(1)).selectCount(any());
        verify(userMapper, times(1)).insert(any());

        // Verify default values are set
        assertEquals(0, newUser.getRole()); // 普通用户
        assertEquals(1, newUser.getStatus()); // 正常
        assertEquals(Integer.valueOf(100), newUser.getCreditScore()); // 默认信用分
    }

    @Test
    void testRegister_Failure_UsernameExists() {
        // Given
        when(userMapper.selectCount(any())).thenReturn(1L); // 用户名已存在

        User newUser = new User();
        newUser.setUsername("existinguser");
        newUser.setPassword("newpassword");

        // When
        Map<String, Object> result = userService.register(newUser);

        // Then
        assertEquals(400, result.get("code"));
        assertEquals("用户名已存在", result.get("message"));
        verify(userMapper, times(1)).selectCount(any());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void testRegister_Failure_SaveFailed() {
        // Given
        when(userMapper.selectCount(any())).thenReturn(0L); // 用户名不存在
        when(userMapper.insert(any())).thenReturn(0); // 插入失败

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");

        // When
        Map<String, Object> result = userService.register(newUser);

        // Then
        assertEquals(500, result.get("code"));
        assertEquals("注册失败", result.get("message"));
        verify(userMapper, times(1)).selectCount(any());
        verify(userMapper, times(1)).insert(any());
    }

    @Test
    void testGetUserById_FromCache() {
        // Given
        Long userId = 1L;
        Map<Object, Object> cachedData = new HashMap<>();
        cachedData.put("id", "1");
        cachedData.put("username", "testuser");
        cachedData.put("nickname", "Test User");
        cachedData.put("role", "0");

        when(hashOps.entries(eq("user:info:1"))).thenReturn(cachedData);

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getNickname());
        assertEquals(0, result.getRole());

        verify(hashOps, times(1)).entries(eq("user:info:1"));
        verify(userMapper, never()).selectById(any());
    }

    @Test
    void testGetUserById_FromDatabase() {
        // Given
        Long userId = 1L;

        when(hashOps.entries(eq("user:info:1"))).thenReturn(new HashMap<>()); // 空缓存
        when(userMapper.selectById(eq(userId))).thenReturn(testUser);

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getNickname(), result.getNickname());
        assertEquals(testUser.getRole(), result.getRole());

        verify(hashOps, times(1)).entries(eq("user:info:1"));
        verify(userMapper, times(1)).selectById(eq(userId));
        verify(hashOps, times(1)).putAll(eq("user:info:1"), any(Map.class));
    }

    @Test
    void testClearCache() {
        // Given
        Long userId = 1L;

        // When
        userService.clearCache(userId);

        // Then
        verify(redisTemplate, times(1)).delete(eq("user:info:1"));
    }
}