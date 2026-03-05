package com.cokosk.homeserve.controller;

import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单控制器 - 包含抢单接口
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 抢单接口 - 核心高并发接口
     * 使用Redis分布式锁 + 限流
     */
    @PostMapping("/grab")
    public Map<String, Object> grabOrder(
            @RequestParam Long orderId,
            @RequestParam Long workerId,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取客户端IP用于限流
        String clientIp = getClientIp(request);
        log.info("收到抢单请求: orderId={}, workerId={}, ip={}", orderId, workerId, clientIp);
        
        // 执行抢单逻辑
        String message = orderService.grabOrder(orderId, workerId, clientIp);
        
        if (message.contains("成功")) {
            result.put("code", 200);
            result.put("message", message);
        } else {
            result.put("code", 400);
            result.put("message", message);
        }
        
        return result;
    }
    
    /**
     * 获取抢单池中的订单
     */
    @GetMapping("/grab-pool")
    public Map<String, Object> getGrabPool() {
        Map<String, Object> result = new HashMap<>();
        java.util.List<Object> orders = orderService.getGrabPoolOrders();
        result.put("code", 200);
        result.put("data", orders);
        return result;
    }
    
    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody Order order) {
        Map<String, Object> result = new HashMap<>();
        // 生成订单号
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setStatus(0); // 待抢单
        order.setPayStatus(0); // 未支付
        
        boolean saved = orderService.save(order);
        
        if (saved) {
            // 添加到Redis抢单池
            orderService.addToGrabPool(order);
            result.put("code", 200);
            result.put("message", "订单创建成功");
            result.put("data", order);
        } else {
            result.put("code", 400);
            result.put("message", "订单创建失败");
        }
        
        return result;
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}