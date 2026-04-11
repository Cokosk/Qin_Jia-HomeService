package com.cokosk.homeserve.controller;

import com.cokosk.homeserve.entity.ServiceCategory;
import com.cokosk.homeserve.entity.ServiceItem;
import com.cokosk.homeserve.service.ServiceCategoryService;
import com.cokosk.homeserve.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务控制器
 * 包含Redis缓存热点数据
 */
@Slf4j
@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
public class ServiceController {
    
    private final ServiceCategoryService categoryService;
    private final ServiceItemService serviceItemService;
    
    /**
     * 获取服务分类列表（Redis缓存）
     */
    @GetMapping("/category")
    public Map<String, Object> getCategories() {
        Map<String, Object> result = new HashMap<>();
        
        List<ServiceCategory> categories = categoryService.getCategoryList();
        
        result.put("code", 200);
        result.put("data", categories);
        
        return result;
    }
    
    /**
     * 获取热门服务列表（Redis缓存）
     */
    @GetMapping("/hot")
    public Map<String, Object> getHotServices() {
        Map<String, Object> result = new HashMap<>();
        
        List<ServiceItem> hotServices = serviceItemService.getHotServices();
        
        result.put("code", 200);
        result.put("data", hotServices);
        
        return result;
    }
    
    /**
     * 根据分类获取服务列表（Redis缓存）
     */
    @GetMapping("/list")
    public Map<String, Object> getServiceList(@RequestParam(required = false) Long categoryId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ServiceItem> services;
        if (categoryId != null) {
            services = serviceItemService.getServicesByCategory(categoryId);
        } else {
            // 获取所有上架的服务
            services = serviceItemService.lambdaQuery()
                    .eq(ServiceItem::getStatus, 1)
                    .list();
        }
        
        result.put("code", 200);
        result.put("data", services);
        
        return result;
    }
    
    /**
     * 获取服务详情（Redis缓存）
     */
    @GetMapping("/detail")
    public Map<String, Object> getServiceDetail(@RequestParam Long serviceId) {
        Map<String, Object> result = new HashMap<>();
        
        ServiceItem service = serviceItemService.getServiceDetail(serviceId);
        
        if (service != null) {
            result.put("code", 200);
            result.put("data", service);
        } else {
            result.put("code", 404);
            result.put("message", "服务不存在");
        }
        
        return result;
    }
    
    /**
     * 搜索服务
     */
    @GetMapping("/search")
    public Map<String, Object> searchServices(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId) {
        Map<String, Object> result = new HashMap<>();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "关键词不能为空");
            return result;
        }
        
        List<ServiceItem> services = serviceItemService.searchServices(keyword.trim(), categoryId);
        
        result.put("code", 200);
        result.put("data", services);
        
        return result;
    }
    
    /**
     * 清除服务缓存（管理端）
     */
    @PostMapping("/clear-cache")
    public Map<String, Object> clearCache(@RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) Long serviceId) {
        Map<String, Object> result = new HashMap<>();
        
        serviceItemService.clearServiceCache(categoryId, serviceId);
        
        result.put("code", 200);
        result.put("message", "缓存已清除");
        
        return result;
    }
}