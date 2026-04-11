package com.cokosk.homeserve.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cokosk.homeserve.entity.ServiceItem;
import com.cokosk.homeserve.mapper.ServiceItemMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务项目服务类
 * 使用Redis缓存热点数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceItemService extends ServiceImpl<ServiceItemMapper, ServiceItem> {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 获取热门服务列表（缓存）
     */
    public List<ServiceItem> getHotServices() {
        String cacheKey = "service:hot";
        
        // 尝试从缓存获取
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            try {
                log.debug("从缓存获取热门服务");
                return objectMapper.readValue(cached, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ServiceItem.class));
            } catch (JsonProcessingException e) {
                log.error("解析热门服务缓存失败", e);
                // 清除失效的缓存
                redisTemplate.delete(cacheKey);
            }
        }
        
        // 从数据库查询热门服务（取前10个）
        QueryWrapper<ServiceItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 10");
        List<ServiceItem> list = this.list(queryWrapper);
        
        // 缓存结果
        if (list != null && !list.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(list);
                redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(30));
            } catch (JsonProcessingException e) {
                log.error("序列化热门服务缓存失败", e);
            }
        }
        
        return list != null ? list : new ArrayList<>();
    }
    
    /**
     * 根据分类ID获取服务列表（缓存）
     */
    public List<ServiceItem> getServicesByCategory(Long categoryId) {
        // 参数校验
        if (categoryId == null || categoryId <= 0) {
            return new ArrayList<>();
        }
        
        String cacheKey = "service:category:" + categoryId;
        
        // 尝试从缓存获取
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            try {
                log.debug("从缓存获取分类服务: categoryId={}", categoryId);
                return objectMapper.readValue(cached, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ServiceItem.class));
            } catch (JsonProcessingException e) {
                log.error("解析分类服务缓存失败: categoryId={}", categoryId, e);
                // 清除失效的缓存
                redisTemplate.delete(cacheKey);
            }
        }
        
        // 从数据库查询
        QueryWrapper<ServiceItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", categoryId);
        queryWrapper.eq("status", 1);
        List<ServiceItem> list = this.list(queryWrapper);
        
        // 缓存结果（30分钟）
        if (list != null && !list.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(list);
                redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(30));
            } catch (JsonProcessingException e) {
                log.error("序列化分类服务缓存失败: categoryId={}", categoryId, e);
            }
        }
        
        return list != null ? list : new ArrayList<>();
    }
    
    /**
     * 获取服务详情（缓存）
     */
    public ServiceItem getServiceDetail(Long serviceId) {
        // 参数校验
        if (serviceId == null || serviceId <= 0) {
            return null;
        }
        
        String cacheKey = "service:detail:" + serviceId;
        
        // 尝试从缓存获取
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            try {
                log.debug("从缓存获取服务详情: serviceId={}", serviceId);
                return objectMapper.readValue(cached, ServiceItem.class);
            } catch (JsonProcessingException e) {
                log.error("解析服务详情缓存失败: serviceId={}", serviceId, e);
                // 清除失效的缓存
                redisTemplate.delete(cacheKey);
            }
        }
        
        // 从数据库查询
        ServiceItem service = this.getById(serviceId);
        
        // 缓存结果（1小时）
        if (service != null) {
            try {
                String json = objectMapper.writeValueAsString(service);
                redisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(1));
            } catch (JsonProcessingException e) {
                log.error("序列化服务详情缓存失败: serviceId={}", serviceId, e);
            }
        }
        
        return service;
    }
    
    /**
     * 分页查询服务列表
     */
    public Page<ServiceItem> getServicePage(Long categoryId, Integer pageNum, Integer pageSize) {
        int page = pageNum != null && pageNum > 0 ? pageNum : 1;
        int size = pageSize != null && pageSize > 0 && pageSize <= 50 ? pageSize : 10;
        
        Page<ServiceItem> pageParam = new Page<>(page, size);
        QueryWrapper<ServiceItem> queryWrapper = new QueryWrapper<>();
        
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq("category_id", categoryId);
        }
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("create_time");
        
        return this.page(pageParam, queryWrapper);
    }
    
    /**
     * 清除服务相关缓存
     */
    public void clearServiceCache(Long categoryId, Long serviceId) {
        if (categoryId != null && categoryId > 0) {
            redisTemplate.delete("service:category:" + categoryId);
        }
        if (serviceId != null && serviceId > 0) {
            redisTemplate.delete("service:detail:" + serviceId);
        }
        // 清除热门服务缓存
        redisTemplate.delete("service:hot");
    }
    
    /**
     * 搜索服务
     */
    public List<ServiceItem> searchServices(String keyword, Long categoryId) {
        QueryWrapper<ServiceItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("name", keyword)
                .or()
                .like("description", keyword));
        }
        
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq("category_id", categoryId);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return this.list(queryWrapper);
    }
    
    /**
     * 更新服务后清除缓存
     */
    public boolean updateService(ServiceItem service) {
        // 参数校验
        if (service == null || service.getId() == null) {
            return false;
        }
        
        boolean updated = this.updateById(service);
        if (updated) {
            clearServiceCache(service.getCategoryId(), service.getId());
        }
        return updated;
    }
}
