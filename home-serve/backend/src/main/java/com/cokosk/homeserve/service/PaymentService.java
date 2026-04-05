package com.cokosk.homeserve.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.entity.Payment;
import com.cokosk.homeserve.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {
    
    private final StringRedisTemplate redisTemplate;
    private final OrderService orderService;
    
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(Long orderId, Long userId, Integer payMethod) {
        Map<String, Object> result = new HashMap<>();
        
        if (orderId == null || userId == null || payMethod == null) {
            result.put("code", 400);
            result.put("message", "参数无效");
            return result;
        }
        
        Order order = orderService.getById(orderId);
        if (order == null) {
            result.put("code", 404);
            result.put("message", "订单不存在");
            return result;
        }
        
        if (order.getPayStatus() == 1) {
            result.put("code", 400);
            result.put("message", "订单已支付");
            return result;
        }
        
        Payment payment = new Payment();
        payment.setPaymentNo("PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(order.getPrice());
        payment.setPayMethod(payMethod);
        payment.setStatus(0);
        
        boolean saved = this.save(payment);
        if (saved) {
            result.put("code", 200);
            result.put("data", payment);
        } else {
            result.put("code", 500);
            result.put("message", "创建支付订单失败");
        }
        return result;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mockPay(String paymentNo, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        Payment payment = findPaymentByNo(paymentNo);
        if (payment == null) {
            result.put("code", 404);
            result.put("message", "支付订单不存在");
            return result;
        }
        
        if (payment.getStatus() == 1) {
            result.put("code", 400);
            result.put("message", "已支付");
            return result;
        }
        
        payment.setStatus(1);
        payment.setTransactionId("MOCK_TXN_" + System.currentTimeMillis());
        payment.setPayTime(LocalDateTime.now());
        this.updateById(payment);
        
        Order order = orderService.getById(payment.getOrderId());
        if (order != null) {
            order.setPayStatus(1);
            order.setPayTime(LocalDateTime.now());
            orderService.updateById(order);
        }
        
        result.put("code", 200);
        result.put("message", "支付成功");
        return result;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mockPayCallback(String paymentNo, String transactionId) {
        return mockPay(paymentNo, null);
    }
    
    public Map<String, Object> queryPayment(String paymentNo) {
        Map<String, Object> result = new HashMap<>();
        Payment payment = findPaymentByNo(paymentNo);
        if (payment == null) {
            result.put("code", 404);
            result.put("message", "支付订单不存在");
        } else {
            result.put("code", 200);
            result.put("data", payment);
        }
        return result;
    }
    
    public Map<String, Object> refund(String paymentNo, Long userId, String reason) {
        Map<String, Object> result = new HashMap<>();
        Payment payment = findPaymentByNo(paymentNo);
        if (payment == null) {
            result.put("code", 404);
            result.put("message", "支付订单不存在");
            return result;
        }
        
        if (payment.getStatus() != 1) {
            result.put("code", 400);
            result.put("message", "该订单未支付，无法退款");
            return result;
        }
        
        payment.setStatus(2);
        payment.setRefundTime(LocalDateTime.now());
        payment.setRefundAmount(payment.getAmount());
        this.updateById(payment);
        
        result.put("code", 200);
        result.put("message", "退款成功");
        return result;
    }
    
    // ========== 新增接口方法 ==========
    
    /**
     * 按订单查询支付状态（新增）
     */
    public Map<String, Object> getPaymentByOrderId(Long orderId) {
        Map<String, Object> result = new HashMap<>();
        
        if (orderId == null || orderId <= 0) {
            result.put("code", 400);
            result.put("message", "订单ID无效");
            return result;
        }
        
        Payment payment = findPaymentByOrderId(orderId);
        
        if (payment == null) {
            result.put("code", 404);
            result.put("message", "该订单暂无支付记录");
            result.put("payStatus", 0);
        } else {
            result.put("code", 200);
            result.put("data", payment);
            result.put("payStatus", payment.getStatus());
        }
        return result;
    }
    
    private Payment findPaymentByNo(String paymentNo) {
        if (paymentNo == null) return null;
        return this.getOne(new QueryWrapper<Payment>().eq("payment_no", paymentNo));
    }
    
    private Payment findPaymentByOrderId(Long orderId) {
        if (orderId == null) return null;
        return this.getOne(new QueryWrapper<Payment>()
            .eq("order_id", orderId)
            .orderByDesc("create_time")
            .last("LIMIT 1"));
    }
    
    /**
     * 根据订单ID获取Payment对象（供其他Service调用）
     */
    public Payment findPaymentEntityByOrderId(Long orderId) {
        return findPaymentByOrderId(orderId);
    }
}
