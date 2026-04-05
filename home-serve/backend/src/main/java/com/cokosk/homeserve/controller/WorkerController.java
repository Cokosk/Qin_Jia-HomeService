package com.cokosk.homeserve.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.entity.Review;
import com.cokosk.homeserve.entity.User;
import com.cokosk.homeserve.service.OrderService;
import com.cokosk.homeserve.service.ReviewService;
import com.cokosk.homeserve.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工人端专用控制器
 * 补充接口：统计数据、最近订单、抢单池详情
 */
@Slf4j
@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {
    
    private final OrderService orderService;
    private final UserService userService;
    private final ReviewService reviewService;
    
    /**
     * 获取工人统计数据
     * GET /api/worker/stats?workerId=xxx
     * 返回：inProgress(进行中)、completed(已完成)、totalIncome(累计收入)
     */
    @GetMapping("/stats")
    public Map<String, Object> getWorkerStats(@RequestParam Long workerId) {
        Map<String, Object> result = new HashMap<>();
        
        if (workerId == null || workerId <= 0) {
            result.put("code", 400);
            result.put("message", "工人ID无效");
            return result;
        }
        
        // 统计进行中订单数量 (status=2)
        long inProgress = orderService.count(new QueryWrapper<Order>()
            .eq("worker_id", workerId)
            .eq("status", 2));
        
        // 统计已完成订单数量 (status=3)
        long completed = orderService.count(new QueryWrapper<Order>()
            .eq("worker_id", workerId)
            .eq("status", 3)
            .eq("pay_status", 1)); // 已支付才算完成
        
        // 累计收入：已完成且已支付的订单金额总和
        Double totalIncome = orderService.lambdaQuery()
            .eq(Order::getWorkerId, workerId)
            .eq(Order::getStatus, 3)
            .eq(Order::getPayStatus, 1)
            .list()
            .stream()
            .mapToDouble(o -> o.getPrice() != null ? o.getPrice().doubleValue() : 0)
            .sum();
        
        // 平均评分
        Double avgRating = reviewService.getAvgRatingByWorkerId(workerId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("inProgress", inProgress);
        stats.put("completed", completed);
        stats.put("totalIncome", Math.round(totalIncome));
        stats.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 5.0);
        
        result.put("code", 200);
        result.put("data", stats);
        
        return result;
    }
    
    /**
     * 获取工人最近订单
     * GET /api/worker/recent-orders?workerId=xxx&limit=5
     */
    @GetMapping("/recent-orders")
    public Map<String, Object> getRecentOrders(
            @RequestParam Long workerId,
            @RequestParam(defaultValue = "5") Integer limit) {
        Map<String, Object> result = new HashMap<>();
        
        if (workerId == null || workerId <= 0) {
            result.put("code", 400);
            result.put("message", "工人ID无效");
            return result;
        }
        
        // 最近订单（按创建时间倒序）
        int size = limit > 0 && limit <= 20 ? limit : 5;
        Page<Order> page = new Page<>(1, size);
        Page<Order> orderPage = orderService.page(page, 
            new QueryWrapper<Order>()
                .eq("worker_id", workerId)
                .orderByDesc("create_time"));
        
        result.put("code", 200);
        result.put("data", orderPage.getRecords());
        
        return result;
    }
    
    /**
     * 获取抢单池详情（含完整订单信息）
     * GET /api/worker/grab-pool
     * 前端需要完整订单信息而非仅ID
     */
    @GetMapping("/grab-pool")
    public Map<String, Object> getGrabPoolDetail() {
        Map<String, Object> result = new HashMap<>();
        
        // 从Redis抢单池获取订单ID列表
        List<Object> orderIds = orderService.getGrabPoolOrders();
        
        if (orderIds == null || orderIds.isEmpty()) {
            result.put("code", 200);
            result.put("data", List.of());
            return result;
        }
        
        // 根据ID列表查询完整订单信息
        List<Long> ids = orderIds.stream()
            .map(id -> Long.parseLong(id.toString()))
            .toList();
        
        List<Order> orders = orderService.lambdaQuery()
            .in(Order::getId, ids)
            .eq(Order::getStatus, 0) // 只返回待抢单状态
            .list();
        
        result.put("code", 200);
        result.put("data", orders);
        
        return result;
    }
    
    /**
     * 获取工人详情（含评分、订单数等）
     * GET /api/worker/detail?workerId=xxx
     */
    @GetMapping("/detail")
    public Map<String, Object> getWorkerDetail(@RequestParam Long workerId) {
        Map<String, Object> result = new HashMap<>();
        
        if (workerId == null || workerId <= 0) {
            result.put("code", 400);
            result.put("message", "工人ID无效");
            return result;
        }
        
        User worker = userService.getById(workerId);
        if (worker == null) {
            result.put("code", 404);
            result.put("message", "工人不存在");
            return result;
        }
        
        // 补充统计数据
        long orderCount = orderService.count(new QueryWrapper<Order>()
            .eq("worker_id", workerId)
            .in("status", 2, 3)); // 服务中或已完成
        
        Double avgRating = reviewService.getAvgRatingByWorkerId(workerId);
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", worker.getId());
        detail.put("nickname", worker.getNickname());
        detail.put("avatar", worker.getAvatar());
        detail.put("creditScore", worker.getCreditScore());
        detail.put("orderCount", orderCount);
        detail.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 5.0);
        
        result.put("code", 200);
        result.put("data", detail);
        
        return result;
    }
}
