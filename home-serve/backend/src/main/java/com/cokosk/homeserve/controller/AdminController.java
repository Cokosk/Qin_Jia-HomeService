package com.cokosk.homeserve.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.entity.Review;
import com.cokosk.homeserve.entity.ServiceCategory;
import com.cokosk.homeserve.entity.ServiceItem;
import com.cokosk.homeserve.entity.User;
import com.cokosk.homeserve.service.OrderService;
import com.cokosk.homeserve.service.ReviewService;
import com.cokosk.homeserve.service.ServiceCategoryService;
import com.cokosk.homeserve.service.ServiceItemService;
import com.cokosk.homeserve.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台控制器
 * 包含用户管理、服务管理、订单管理、评价管理等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    private final ServiceCategoryService categoryService;
    private final ServiceItemService serviceItemService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    
    // ==================== 用户管理 ====================
    
    /**
     * 用户列表（分页）
     * GET /api/admin/user/list
     */
    @GetMapping("/user/list")
    public Map<String, Object> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        int page = pageNum > 0 ? pageNum : 1;
        int size = pageSize > 0 && pageSize <= 50 ? pageSize : 10;
        
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        if (role != null && role >= 0 && role <= 2) {
            queryWrapper.eq("role", role);
        }
        if (status != null && (status == 0 || status == 1)) {
            queryWrapper.eq("status", status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("username", keyword.trim())
                .or()
                .like("nickname", keyword.trim())
                .or()
                .like("phone", keyword.trim())
            );
        }
        queryWrapper.orderByDesc("create_time");
        
        Page<User> userPage = userService.page(pageParam, queryWrapper);
        
        result.put("code", 200);
        result.put("data", userPage.getRecords());
        result.put("total", userPage.getTotal());
        
        return result;
    }
    
    /**
     * 用户详情
     * GET /api/admin/user/detail?userId=xxx
     */
    @GetMapping("/user/detail")
    public Map<String, Object> getUserDetail(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        
        User user = userService.getById(userId);
        
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
     * 更新用户状态（启用/禁用）
     * POST /api/admin/user/status
     */
    @PostMapping("/user/status")
    public Map<String, Object> updateUserStatus(
            @RequestParam Long userId,
            @RequestParam Integer status) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        if (status == null || (status != 0 && status != 1)) {
            result.put("code", 400);
            result.put("message", "状态值无效");
            return result;
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }
        
        // 禁止修改管理员状态
        if (user.getRole() != null && user.getRole() == 2) {
            result.put("code", 403);
            result.put("message", "无法修改管理员状态");
            return result;
        }
        
        user.setStatus(status);
        boolean updated = userService.updateById(user);
        
        if (updated) {
            // 清除用户缓存
            userService.clearCache(userId);
            log.info("管理员更新用户状态: userId={}, status={}", userId, status);
            
            result.put("code", 200);
            result.put("message", status == 1 ? "用户已启用" : "用户已禁用");
        } else {
            result.put("code", 500);
            result.put("message", "操作失败");
        }
        
        return result;
    }
    
    /**
     * 更新用户角色
     * POST /api/admin/user/role
     */
    @PostMapping("/user/role")
    public Map<String, Object> updateUserRole(
            @RequestParam Long userId,
            @RequestParam Integer role) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        if (role == null || role < 0 || role > 2) {
            result.put("code", 400);
            result.put("message", "角色值无效");
            return result;
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }
        
        user.setRole(role);
        boolean updated = userService.updateById(user);
        
        if (updated) {
            userService.clearCache(userId);
            log.info("管理员更新用户角色: userId={}, role={}", userId, role);
            
            result.put("code", 200);
            result.put("message", "角色已更新");
        } else {
            result.put("code", 500);
            result.put("message", "操作失败");
        }
        
        return result;
    }
    
    /**
     * 调整用户信用分
     * POST /api/admin/user/credit
     */
    @PostMapping("/user/credit")
    public Map<String, Object> adjustUserCredit(
            @RequestParam Long userId,
            @RequestParam Integer creditScore) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (userId == null || userId <= 0) {
            result.put("code", 400);
            result.put("message", "用户ID无效");
            return result;
        }
        if (creditScore == null || creditScore < 0 || creditScore > 200) {
            result.put("code", 400);
            result.put("message", "信用分范围0-200");
            return result;
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            result.put("code", 404);
            result.put("message", "用户不存在");
            return result;
        }
        
        user.setCreditScore(creditScore);
        boolean updated = userService.updateById(user);
        
        if (updated) {
            userService.clearCache(userId);
            log.info("管理员调整用户信用分: userId={}, creditScore={}", userId, creditScore);
            
            result.put("code", 200);
            result.put("message", "信用分已更新");
        } else {
            result.put("code", 500);
            result.put("message", "操作失败");
        }
        
        return result;
    }
    
    // ==================== 服务管理 ====================
    
    /**
     * 服务列表（分页）
     * GET /api/admin/service/list
     */
    @GetMapping("/service/list")
    public Map<String, Object> getServiceList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        Map<String, Object> result = new HashMap<>();
        
        int page = pageNum > 0 ? pageNum : 1;
        int size = pageSize > 0 && pageSize <= 50 ? pageSize : 10;
        
        Page<ServiceItem> pageParam = new Page<>(page, size);
        QueryWrapper<ServiceItem> queryWrapper = new QueryWrapper<>();
        
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq("category_id", categoryId);
        }
        if (status != null && (status == 0 || status == 1)) {
            queryWrapper.eq("status", status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("name", keyword.trim());
        }
        queryWrapper.orderByDesc("create_time");
        
        Page<ServiceItem> servicePage = serviceItemService.page(pageParam, queryWrapper);
        
        result.put("code", 200);
        result.put("data", servicePage.getRecords());
        result.put("total", servicePage.getTotal());
        
        return result;
    }
    
    /**
     * 添加服务
     * POST /api/admin/service/add
     */
    @PostMapping("/service/add")
    public Map<String, Object> addService(@RequestBody ServiceItem service) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (service == null) {
            result.put("code", 400);
            result.put("message", "服务信息无效");
            return result;
        }
        if (service.getName() == null || service.getName().isEmpty()) {
            result.put("code", 400);
            result.put("message", "服务名称不能为空");
            return result;
        }
        if (service.getCategoryId() == null || service.getCategoryId() <= 0) {
            result.put("code", 400);
            result.put("message", "分类ID无效");
            return result;
        }
        if (service.getPrice() == null || service.getPrice().doubleValue() <= 0) {
            result.put("code", 400);
            result.put("message", "价格无效");
            return result;
        }
        
        // 设置默认状态
        if (service.getStatus() == null) {
            service.setStatus(1);
        }
        
        boolean saved = serviceItemService.save(service);
        
        if (saved) {
            // 清除缓存
            serviceItemService.clearServiceCache(service.getCategoryId(), null);
            log.info("管理员添加服务: name={}, categoryId={}", service.getName(), service.getCategoryId());
            
            result.put("code", 200);
            result.put("message", "服务添加成功");
            result.put("data", service);
        } else {
            result.put("code", 500);
            result.put("message", "添加失败");
        }
        
        return result;
    }
    
    /**
     * 更新服务
     * POST /api/admin/service/update
     */
    @PostMapping("/service/update")
    public Map<String, Object> updateService(@RequestBody ServiceItem service) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (service == null || service.getId() == null || service.getId() <= 0) {
            result.put("code", 400);
            result.put("message", "服务ID无效");
            return result;
        }
        
        ServiceItem existService = serviceItemService.getById(service.getId());
        if (existService == null) {
            result.put("code", 404);
            result.put("message", "服务不存在");
            return result;
        }
        
        boolean updated = serviceItemService.updateById(service);
        
        if (updated) {
            // 清除缓存
            Long categoryId = service.getCategoryId() != null ? service.getCategoryId() : existService.getCategoryId();
            serviceItemService.clearServiceCache(categoryId, service.getId());
            log.info("管理员更新服务: id={}", service.getId());
            
            result.put("code", 200);
            result.put("message", "服务更新成功");
        } else {
            result.put("code", 500);
            result.put("message", "更新失败");
        }
        
        return result;
    }
    
    /**
     * 更新服务状态（上架/下架）
     * POST /api/admin/service/status
     */
    @PostMapping("/service/status")
    public Map<String, Object> updateServiceStatus(
            @RequestParam Long serviceId,
            @RequestParam Integer status) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (serviceId == null || serviceId <= 0) {
            result.put("code", 400);
            result.put("message", "服务ID无效");
            return result;
        }
        if (status == null || (status != 0 && status != 1)) {
            result.put("code", 400);
            result.put("message", "状态值无效");
            return result;
        }
        
        ServiceItem service = serviceItemService.getById(serviceId);
        if (service == null) {
            result.put("code", 404);
            result.put("message", "服务不存在");
            return result;
        }
        
        service.setStatus(status);
        boolean updated = serviceItemService.updateById(service);
        
        if (updated) {
            serviceItemService.clearServiceCache(service.getCategoryId(), serviceId);
            log.info("管理员更新服务状态: serviceId={}, status={}", serviceId, status);
            
            result.put("code", 200);
            result.put("message", status == 1 ? "服务已上架" : "服务已下架");
        } else {
            result.put("code", 500);
            result.put("message", "操作失败");
        }
        
        return result;
    }
    
    /**
     * 删除服务
     * POST /api/admin/service/delete
     */
    @PostMapping("/service/delete")
    public Map<String, Object> deleteService(@RequestParam Long serviceId) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (serviceId == null || serviceId <= 0) {
            result.put("code", 400);
            result.put("message", "服务ID无效");
            return result;
        }
        
        ServiceItem service = serviceItemService.getById(serviceId);
        if (service == null) {
            result.put("code", 404);
            result.put("message", "服务不存在");
            return result;
        }
        
        boolean deleted = serviceItemService.removeById(serviceId);
        
        if (deleted) {
            serviceItemService.clearServiceCache(service.getCategoryId(), serviceId);
            log.info("管理员删除服务: serviceId={}", serviceId);
            
            result.put("code", 200);
            result.put("message", "服务已删除");
        } else {
            result.put("code", 500);
            result.put("message", "删除失败");
        }
        
        return result;
    }
    
    // ==================== 订单管理 ====================
    
    /**
     * 订单列表（分页）
     * GET /api/admin/order/list
     */
    @GetMapping("/order/list")
    public Map<String, Object> getOrderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long workerId) {
        
        Map<String, Object> result = new HashMap<>();
        
        int page = pageNum > 0 ? pageNum : 1;
        int size = pageSize > 0 && pageSize <= 50 ? pageSize : 10;
        
        Page<Order> pageParam = new Page<>(page, size);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        
        if (status != null && status >= 0 && status <= 4) {
            queryWrapper.eq("status", status);
        }
        if (userId != null && userId > 0) {
            queryWrapper.eq("user_id", userId);
        }
        if (workerId != null && workerId > 0) {
            queryWrapper.eq("worker_id", workerId);
        }
        queryWrapper.orderByDesc("create_time");
        
        Page<Order> orderPage = orderService.page(pageParam, queryWrapper);
        
        result.put("code", 200);
        result.put("data", orderPage.getRecords());
        result.put("total", orderPage.getTotal());
        
        return result;
    }
    
    /**
     * 强制取消订单
     * POST /api/admin/order/cancel
     */
    @PostMapping("/order/cancel")
    public Map<String, Object> cancelOrder(@RequestParam Long orderId) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (orderId == null || orderId <= 0) {
            result.put("code", 400);
            result.put("message", "订单ID无效");
            return result;
        }
        
        Order order = orderService.getById(orderId);
        if (order == null) {
            result.put("code", 404);
            result.put("message", "订单不存在");
            return result;
        }
        
        // 只有待抢单和已接单状态可以取消
        if (order.getStatus() != null && order.getStatus() > 2) {
            result.put("code", 400);
            result.put("message", "该订单状态无法取消");
            return result;
        }
        
        order.setStatus(4);
        boolean updated = orderService.updateById(order);
        
        if (updated) {
            // 清除Redis抢单池缓存
            orderService.clearCache(orderId);
            log.info("管理员强制取消订单: orderId={}", orderId);
            
            result.put("code", 200);
            result.put("message", "订单已取消");
        } else {
            result.put("code", 500);
            result.put("message", "操作失败");
        }
        
        return result;
    }
    
    // ==================== 评价管理 ====================
    
    /**
     * 评价列表（分页）
     * GET /api/admin/review/list
     */
    @GetMapping("/review/list")
    public Map<String, Object> getReviewList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer rating) {
        
        Map<String, Object> result = new HashMap<>();
        
        int page = pageNum > 0 ? pageNum : 1;
        int size = pageSize > 0 && pageSize <= 50 ? pageSize : 10;
        
        Page<Review> pageParam = new Page<>(page, size);
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        
        if (status != null && (status == 0 || status == 1)) {
            queryWrapper.eq("status", status);
        }
        if (rating != null && rating >= 1 && rating <= 5) {
            queryWrapper.eq("rating", rating);
        }
        queryWrapper.orderByDesc("create_time");
        
        Page<Review> reviewPage = reviewService.page(pageParam, queryWrapper);
        
        result.put("code", 200);
        result.put("data", reviewPage.getRecords());
        result.put("total", reviewPage.getTotal());
        
        return result;
    }
    
    /**
     * 隐藏/显示评价
     * POST /api/admin/review/status
     */
    @PostMapping("/review/status")
    public Map<String, Object> updateReviewStatus(
            @RequestParam Long reviewId,
            @RequestParam Integer status) {
        
        log.info("管理员更新评价状态: reviewId={}, status={}", reviewId, status);
        return reviewService.toggleReviewStatus(reviewId, status);
    }
    
    // ==================== 统计数据 ====================
    
    /**
     * 获取系统统计数据
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getSystemStats() {
        
        Map<String, Object> result = new HashMap<>();
        
        // 用户统计
        long totalUsers = userService.count();
        long userCount = userService.lambdaQuery().eq(User::getRole, 0).count();
        long workerCount = userService.lambdaQuery().eq(User::getRole, 1).count();
        
        // 服务统计
        long totalServices = serviceItemService.count();
        long activeServices = serviceItemService.lambdaQuery().eq(ServiceItem::getStatus, 1).count();
        
        // 订单统计
        long totalOrders = orderService.count();
        long pendingOrders = orderService.lambdaQuery().eq(Order::getStatus, 0).count();
        long inProgressOrders = orderService.lambdaQuery().in(Order::getStatus, 1, 2).count();
        long completedOrders = orderService.lambdaQuery().eq(Order::getStatus, 3).count();
        
        // 评价统计
        long totalReviews = reviewService.count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("userCount", userCount);
        stats.put("workerCount", workerCount);
        stats.put("totalServices", totalServices);
        stats.put("activeServices", activeServices);
        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("inProgressOrders", inProgressOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("totalReviews", totalReviews);
        
        result.put("code", 200);
        result.put("data", stats);
        
        return result;
    }
    
    // ==================== 分类管理 ====================
    
    /**
     * 分类列表
     * GET /api/admin/category/list
     */
    @GetMapping("/category/list")
    public Map<String, Object> getCategoryList() {
        Map<String, Object> result = new HashMap<>();
        
        List<ServiceCategory> categories = categoryService.list(
            new QueryWrapper<ServiceCategory>().orderByAsc("sort")
        );
        
        result.put("code", 200);
        result.put("data", categories);
        
        return result;
    }
    
    /**
     * 添加分类
     * POST /api/admin/category/add
     */
    @PostMapping("/category/add")
    public Map<String, Object> addCategory(@RequestBody ServiceCategory category) {
        Map<String, Object> result = new HashMap<>();
        
        if (category.getName() == null || category.getName().isEmpty()) {
            result.put("code", 400);
            result.put("message", "分类名称不能为空");
            return result;
        }
        
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        boolean saved = categoryService.save(category);
        
        if (saved) {
            categoryService.clearCache();
            log.info("管理员添加分类: name={}", category.getName());
            result.put("code", 200);
            result.put("message", "分类添加成功");
            result.put("data", category);
        } else {
            result.put("code", 500);
            result.put("message", "添加失败");
        }
        
        return result;
    }
    
    /**
     * 更新分类
     * POST /api/admin/category/update
     */
    @PostMapping("/category/update")
    public Map<String, Object> updateCategory(@RequestBody ServiceCategory category) {
        Map<String, Object> result = new HashMap<>();
        
        if (category.getId() == null || category.getId() <= 0) {
            result.put("code", 400);
            result.put("message", "分类ID无效");
            return result;
        }
        
        ServiceCategory existCategory = categoryService.getById(category.getId());
        if (existCategory == null) {
            result.put("code", 404);
            result.put("message", "分类不存在");
            return result;
        }
        
        boolean updated = categoryService.updateCategory(category);
        
        if (updated) {
            categoryService.clearCache();
            log.info("管理员更新分类: id={}", category.getId());
            result.put("code", 200);
            result.put("message", "分类更新成功");
        } else {
            result.put("code", 500);
            result.put("message", "更新失败");
        }
        
        return result;
    }
    
    /**
     * 删除分类
     * POST /api/admin/category/delete
     */
    @PostMapping("/category/delete")
    public Map<String, Object> deleteCategory(@RequestParam Long categoryId) {
        Map<String, Object> result = new HashMap<>();
        
        if (categoryId == null || categoryId <= 0) {
            result.put("code", 400);
            result.put("message", "分类ID无效");
            return result;
        }
        
        ServiceCategory category = categoryService.getById(categoryId);
        if (category == null) {
            result.put("code", 404);
            result.put("message", "分类不存在");
            return result;
        }
        
        boolean deleted = categoryService.removeById(categoryId);
        
        if (deleted) {
            categoryService.clearCache();
            log.info("管理员删除分类: categoryId={}", categoryId);
            result.put("code", 200);
            result.put("message", "分类已删除");
        } else {
            result.put("code", 500);
            result.put("message", "删除失败");
        }
        
        return result;
    }
    
    /**
     * 更新分类状态
     * POST /api/admin/category/status
     */
    @PostMapping("/category/status")
    public Map<String, Object> updateCategoryStatus(
            @RequestParam Long categoryId,
            @RequestParam Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        if (categoryId == null || categoryId <= 0) {
            result.put("code", 400);
            result.put("message", "分类ID无效");
            return result;
        }
        if (status == null || (status != 0 && status != 1)) {
            result.put("code", 400);
            result.put("message", "状态值无效");
            return result;
        }
        
        ServiceCategory category = categoryService.getById(categoryId);
        if (category == null) {
            result.put("code", 404);
            result.put("message", "分类不存在");
            return result;
        }
        
        category.setStatus(status);
        boolean updated = categoryService.updateById(category);
        
        if (updated) {
            categoryService.clearCache();
            log.info("管理员更新分类状态: categoryId={}, status={}", categoryId, status);
            result.put("code", 200);
            result.put("message", status == 1 ? "分类已启用" : "分类已禁用");
        } else {
            result.put("code", 500);
            result.put("message", "操作失败");
        }
        
        return result;
    }
}
