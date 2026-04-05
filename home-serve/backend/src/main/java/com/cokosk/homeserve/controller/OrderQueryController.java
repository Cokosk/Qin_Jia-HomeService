package com.cokosk.homeserve.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单查询控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderQueryController {
    
    private final OrderService orderService;
    
    /**
     * 用户订单列表
     */
    @GetMapping("/list")
    public Map<String, Object> getOrderList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Map<String, Object> result = new HashMap<>();
        
        Page<Order> page = new Page<>(pageNum, pageSize);
        Page<Order> orderPage = orderService.page(page, 
            new QueryWrapper<Order>().eq("user_id", userId).orderByDesc("create_time"));
        
        result.put("code", 200);
        result.put("data", orderPage.getRecords());
        result.put("total", orderPage.getTotal());
        
        return result;
    }
    
    /**
     * 服务者订单列表（包括历史订单）
     */
    @GetMapping("/worker-list")
    public Map<String, Object> getWorkerOrderList(
            @RequestParam Long workerId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Map<String, Object> result = new HashMap<>();
        
        Page<Order> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<Order>()
            .eq("worker_id", workerId);
        
        // 如果指定了状态，按状态筛选
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        Page<Order> orderPage = orderService.page(page, queryWrapper);
        
        result.put("code", 200);
        result.put("data", orderPage.getRecords());
        result.put("total", orderPage.getTotal());
        
        return result;
    }
    
    /**
     * 订单详情
     */
    @GetMapping("/detail")
    public Map<String, Object> getOrderDetail(@RequestParam Long orderId) {
        Map<String, Object> result = new HashMap<>();
        
        Order order = orderService.getById(orderId);
        
        if (order != null) {
            result.put("code", 200);
            result.put("data", order);
        } else {
            result.put("code", 404);
            result.put("message", "订单不存在");
        }
        
        return result;
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public Map<String, Object> cancelOrder(@RequestParam Long orderId, @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        Order order = orderService.getById(orderId);
        
        if (order == null) {
            result.put("code", 404);
            result.put("message", "订单不存在");
            return result;
        }
        
        // 只有订单创建者和管理员可以取消
        if (!order.getUserId().equals(userId)) {
            result.put("code", 403);
            result.put("message", "无权限操作");
            return result;
        }
        
        // 只有待抢单状态可以取消
        if (order.getStatus() != 0) {
            result.put("code", 400);
            result.put("message", "该订单无法取消");
            return result;
        }
        
        order.setStatus(4); // 已取消
        boolean updated = orderService.updateById(order);
        
        if (updated) {
            // 清除Redis缓存
            orderService.clearCache(orderId);
            result.put("code", 200);
            result.put("message", "订单已取消");
        } else {
            result.put("code", 500);
            result.put("message", "取消失败");
        }
        
        return result;
    }
    
    /**
     * 开始服务
     */
    @PostMapping("/start")
    public Map<String, Object> startService(@RequestParam Long orderId, @RequestParam Long workerId) {
        Map<String, Object> result = new HashMap<>();
        
        Order order = orderService.getById(orderId);
        
        if (order == null) {
            result.put("code", 404);
            result.put("message", "订单不存在");
            return result;
        }
        
        if (!order.getWorkerId().equals(workerId)) {
            result.put("code", 403);
            result.put("message", "无权限操作");
            return result;
        }
        
        if (order.getStatus() != 1) {
            result.put("code", 400);
            result.put("message", "订单状态无法开始服务");
            return result;
        }
        
        order.setStatus(2); // 服务中
        order.setStartTime(java.time.LocalDateTime.now());
        
        boolean updated = orderService.updateById(order);
        
        result.put("code", updated ? 200 : 500);
        result.put("message", updated ? "开始服务成功" : "操作失败");
        
        return result;
    }
    
    /**
     * 完成服务
     */
    @PostMapping("/finish")
    public Map<String, Object> finishService(@RequestParam Long orderId, @RequestParam Long workerId) {
        Map<String, Object> result = new HashMap<>();
        
        Order order = orderService.getById(orderId);
        
        if (order == null) {
            result.put("code", 404);
            result.put("message", "订单不存在");
            return result;
        }
        
        if (!order.getWorkerId().equals(workerId)) {
            result.put("code", 403);
            result.put("message", "无权限操作");
            return result;
        }
        
        if (order.getStatus() != 2) {
            result.put("code", 400);
            result.put("message", "订单状态无法完成");
            return result;
        }
        
        order.setStatus(3); // 已完成
        order.setFinishTime(java.time.LocalDateTime.now());
        
        boolean updated = orderService.updateById(order);
        
        result.put("code", updated ? 200 : 500);
        result.put("message", updated ? "服务已完成" : "操作失败");
        
        return result;
    }
}
