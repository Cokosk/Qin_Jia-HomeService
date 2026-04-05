package com.cokosk.homeserve.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cokosk.homeserve.entity.ServiceItem;
import com.cokosk.homeserve.mapper.ServiceItemMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceItemServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ServiceItemMapper serviceItemMapper;

    @Mock
    private ValueOperations<String, String> valueOps;

    private ObjectMapper objectMapper;

    private ServiceItemService serviceItemService;

    private ServiceItem testService;
    private List<ServiceItem> testServiceList;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        // 创建ServiceItemService实例
        serviceItemService = new ServiceItemService(redisTemplate, objectMapper);
        
        Field baseMapperField = ServiceItemService.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(serviceItemService, serviceItemMapper);

        testService = new ServiceItem();
        testService.setId(1L);
        testService.setCategoryId(1L);
        testService.setName("家庭保洁");
        testService.setDescription("专业家庭保洁服务");
        testService.setPrice(new BigDecimal("100.00"));
        testService.setDuration(2);
        testService.setStatus(1);

        testServiceList = new ArrayList<>();
        testServiceList.add(testService);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void testGetHotServices_FromCache() throws Exception {
        // Given
        String cachedJson = objectMapper.writeValueAsString(testServiceList);
        when(valueOps.get(eq("service:hot"))).thenReturn(cachedJson);

        // When
        List<ServiceItem> result = serviceItemService.getHotServices();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("家庭保洁", result.get(0).getName());

        verify(valueOps, times(1)).get(eq("service:hot"));
        verify(serviceItemMapper, never()).selectList(any());
    }

    @Test
    void testGetHotServices_FromDatabase() {
        // Given
        when(valueOps.get(eq("service:hot"))).thenReturn(null);
        when(serviceItemMapper.selectList(any(QueryWrapper.class))).thenReturn(testServiceList);

        // When
        List<ServiceItem> result = serviceItemService.getHotServices();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("家庭保洁", result.get(0).getName());

        verify(valueOps, times(1)).get(eq("service:hot"));
        verify(serviceItemMapper, times(1)).selectList(any(QueryWrapper.class));
    }

    @Test
    void testGetServiceDetail_Success() throws Exception {
        // Given
        String cachedJson = objectMapper.writeValueAsString(testService);
        when(valueOps.get(eq("service:detail:1"))).thenReturn(cachedJson);

        // When
        ServiceItem result = serviceItemService.getServiceDetail(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("家庭保洁", result.getName());

        verify(valueOps, times(1)).get(eq("service:detail:1"));
        verify(serviceItemMapper, never()).selectById(any());
    }

    @Test
    void testGetServiceDetail_NotFound() {
        // Given
        when(valueOps.get(eq("service:detail:999"))).thenReturn(null);
        when(serviceItemMapper.selectById(999L)).thenReturn(null);

        // When
        ServiceItem result = serviceItemService.getServiceDetail(999L);

        // Then
        assertNull(result);

        verify(valueOps, times(1)).get(eq("service:detail:999"));
        verify(serviceItemMapper, times(1)).selectById(999L);
    }

    @Test
    void testGetServicesByCategory_Success() throws Exception {
        // Given
        String cachedJson = objectMapper.writeValueAsString(testServiceList);
        when(valueOps.get(eq("service:category:1"))).thenReturn(cachedJson);

        // When
        List<ServiceItem> result = serviceItemService.getServicesByCategory(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCategoryId());

        verify(valueOps, times(1)).get(eq("service:category:1"));
    }

    @Test
    void testGetServicesByCategory_FromDatabase() {
        // Given
        when(valueOps.get(eq("service:category:1"))).thenReturn(null);
        when(serviceItemMapper.selectList(any(QueryWrapper.class))).thenReturn(testServiceList);

        // When
        List<ServiceItem> result = serviceItemService.getServicesByCategory(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(serviceItemMapper, times(1)).selectList(any(QueryWrapper.class));
    }

    @Test
    void testClearServiceCache() {
        // When
        serviceItemService.clearServiceCache(1L, 1L);

        // Then
        verify(redisTemplate, times(1)).delete(eq("service:category:1"));
        verify(redisTemplate, times(1)).delete(eq("service:detail:1"));
        verify(redisTemplate, times(1)).delete(eq("service:hot"));
    }
}
