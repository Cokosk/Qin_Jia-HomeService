package com.cokosk.homeserve.controller;

import com.cokosk.homeserve.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付控制器 - Mock实现
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * 创建支付订单
     * POST /api/payment/create
     * payMethod: 1-微信 2-支付宝 3-余额
     */
    @PostMapping("/create")
    public Map<String, Object> createPayment(
            @RequestParam Long orderId,
            @RequestParam Long userId,
            @RequestParam Integer payMethod) {
        log.info("创建支付订单: orderId={}, userId={}, payMethod={}", orderId, userId, payMethod);
        return paymentService.createPayment(orderId, userId, payMethod);
    }
    
    /**
     * Mock支付（测试用）
     * POST /api/payment/mock-pay
     * 直接完成支付，用于测试环境
     */
    @PostMapping("/mock-pay")
    public Map<String, Object> mockPay(
            @RequestParam String paymentNo,
            @RequestParam Long userId) {
        log.info("Mock支付: paymentNo={}, userId={}", paymentNo, userId);
        return paymentService.mockPay(paymentNo, userId);
    }
    
    /**
     * 支付回调（Mock）
     * POST /api/payment/callback
     * 模拟第三方支付平台回调
     */
    @PostMapping("/callback")
    public Map<String, Object> payCallback(
            @RequestParam String paymentNo,
            @RequestParam String transactionId) {
        log.info("支付回调: paymentNo={}, transactionId={}", paymentNo, transactionId);
        return paymentService.mockPayCallback(paymentNo, transactionId);
    }
    
    /**
     * 查询支付状态
     * GET /api/payment/query?paymentNo=xxx
     */
    @GetMapping("/query")
    public Map<String, Object> queryPayment(@RequestParam String paymentNo) {
        return paymentService.queryPayment(paymentNo);
    }
    
    /**
     * 申请退款
     * POST /api/payment/refund
     */
    @PostMapping("/refund")
    public Map<String, Object> refund(
            @RequestParam String paymentNo,
            @RequestParam Long userId,
            @RequestParam(required = false) String reason) {
        log.info("申请退款: paymentNo={}, userId={}", paymentNo, userId);
        return paymentService.refund(paymentNo, userId, reason);
    }
    
    /**
     * 按订单查询支付状态（新增接口）
     * GET /api/payment/status?orderId=xxx
     */
    @GetMapping("/status")
    public Map<String, Object> getPaymentByOrderId(@RequestParam Long orderId) {
        log.info("按订单查询支付状态: orderId={}", orderId);
        return paymentService.getPaymentByOrderId(orderId);
    }
}
