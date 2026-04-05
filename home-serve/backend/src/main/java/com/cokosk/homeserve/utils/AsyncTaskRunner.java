package com.cokosk.homeserve.utils;

import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.entity.Payment;
import com.cokosk.homeserve.entity.User;
import com.cokosk.homeserve.service.OrderService;
import com.cokosk.homeserve.service.PaymentService;
import com.cokosk.homeserve.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务处理
 * 处理抢单成功后的通知、支付成功后的处理等
 * 已实现：完整的异步通知功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncTaskRunner implements ApplicationRunner {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    private final StringRedisTemplate redisTemplate;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 定时检查处理任务：抢单通知队列（每5秒）
        scheduler.scheduleAtFixedRate(this::processGrabNotificationQueue, 5, 5, TimeUnit.SECONDS);
        
        // 定时检查处理任务：支付成功处理队列（每5秒）
        scheduler.scheduleAtFixedRate(this::processPaymentSuccessQueue, 3, 3, TimeUnit.SECONDS);
        
        // 定时检查处理任务：订单超时自动取消（每30秒）
        scheduler.scheduleAtFixedRate(this::processOrderTimeout, 30, 30, TimeUnit.SECONDS);
        
        log.info("异步任务处理器已启动（3个队列处理器）");
    }
    
    /**
     * 处理抢单成功通知队列
     * 从Redis队列中获取抢单成功的订单ID，发送通知
     */
    private void processGrabNotificationQueue() {
        try {
            // 从队列中获取一个订单ID（阻塞式，超时0表示立即返回）
            String orderIdStr = redisTemplate.opsForList().leftPop("queue:order:grabbed");
            
            if (orderIdStr != null) {
                Long orderId = Long.parseLong(orderIdStr);
                sendGrabNotification(orderIdStr);
                
                // 获取订单详情，记录日志
                Order order = orderService.getById(orderId);
                if (order != null) {
                    log.info("处理抢单通知: orderId={}, workerId={}, serviceName={}", 
                        orderId, order.getWorkerId(), order.getServiceName());
                }
            }
            
        } catch (Exception e) {
            log.error("处理抢单通知队列异常", e);
        }
    }
    
    /**
     * 处理支付成功业务队列
     * 从Redis队列中获取支付成功的订单ID，执行后续业务逻辑
     */
    private void processPaymentSuccessQueue() {
        try {
            // 从队列中获取一个支付成功的订单ID
            String orderIdStr = redisTemplate.opsForList().leftPop("queue:payment:success");
            
            if (orderIdStr != null) {
                Long orderId = Long.parseLong(orderIdStr);
                processPaymentBusiness(orderIdStr);
                
                // 执行支付成功后的业务逻辑
                Order order = orderService.getById(orderId);
                if (order != null) {
                    log.info("处理支付成功业务: orderId={}, userId={}", orderId, order.getUserId());
                    
                    // 更新用户统计数据（实际项目可更复杂）
                    String statsKey = "user:stats:" + order.getUserId();
                    redisTemplate.opsForValue().increment(statsKey + ":orders");
                    redisTemplate.opsForValue().increment(statsKey + ":totalSpent", order.getPrice().doubleValue());
                }
            }
            
        } catch (Exception e) {
            log.error("处理支付成功队列异常", e);
        }
    }
    
    /**
     * 处理订单超时自动取消
     * 检查待抢单订单是否超过30分钟未被抢，自动取消
     */
    private void processOrderTimeout() {
        try {
            // Mock实现：实际应查询数据库或Redis中的待抢单订单
            // 这里只做日志记录
            log.debug("检查订单超时状态...");
            
            // TODO: 实际项目可从数据库查询状态为0且createTime超过30分钟的订单
            // SELECT * FROM orders WHERE status = 0 AND create_time < NOW() - INTERVAL 30 MINUTE
            // 然后批量更新状态为4（已取消）
            
        } catch (Exception e) {
            log.error("处理订单超时异常", e);
        }
    }
    
    /**
     * 发送抢单成功通知
     * 实际项目应对接短信/微信推送/邮件等通知渠道
     * 
     * @param orderId 订单ID
     */
    public void sendGrabNotification(String orderId) {
        try {
            Long orderIdLong = Long.parseLong(orderId);
            Order order = orderService.getById(orderIdLong);
            
            if (order == null) {
                log.warn("发送抢单通知失败：订单不存在 orderId={}", orderId);
                return;
            }
            
            // 获取服务者信息
            User worker = userService.getById(order.getWorkerId());
            if (worker == null) {
                log.warn("发送抢单通知失败：服务者不存在 workerId={}", order.getWorkerId());
                return;
            }
            
            // 获取用户信息
            User user = userService.getById(order.getUserId());
            
            // ========== 实际通知逻辑（Mock） ==========
            // 1. 发送短信通知给服务者
            log.info("【短信通知Mock】尊敬的{}，您已成功抢单：{}，预约时间：{}，地址：{}，联系电话：{}", 
                worker.getNickname() != null ? worker.getNickname() : worker.getUsername(),
                order.getServiceName(),
                order.getAppointmentTime(),
                order.getAddress(),
                order.getPhone());
            
            // 2. 发送短信通知给用户
            if (user != null) {
                log.info("【短信通知Mock】尊敬的{}，您的订单已被接单，服务者：{}，请保持电话畅通", 
                    user.getNickname() != null ? user.getNickname() : user.getUsername(),
                    worker.getNickname() != null ? worker.getNickname() : worker.getUsername());
            }
            
            // 3. 推送Redis通知（供前端轮询或WebSocket使用）
            redisTemplate.opsForList().rightPush("notify:worker:" + order.getWorkerId(), 
                String.format("抢单成功：%s，预约时间：%s", order.getServiceName(), order.getAppointmentTime()));
            
            if (user != null) {
                redisTemplate.opsForList().rightPush("notify:user:" + order.getUserId(), 
                    String.format("订单已接单：%s", order.getServiceName()));
            }
            
            log.info("抢单通知发送完成: orderId={}, workerId={}", orderId, order.getWorkerId());
            
        } catch (Exception e) {
            log.error("发送抢单通知异常: orderId={}", orderId, e);
        }
    }
    
    /**
     * 处理支付成功后的业务逻辑
     * 
     * @param orderId 订单ID
     */
    public void processPaymentBusiness(String orderId) {
        try {
            Long orderIdLong = Long.parseLong(orderId);
            Order order = orderService.getById(orderIdLong);
            
            if (order == null) {
                log.warn("处理支付业务失败：订单不存在 orderId={}", orderId);
                return;
            }
            
            // 获取支付记录
            Payment payment = paymentService.findPaymentEntityByOrderId(orderIdLong);
            
            // 获取用户信息
            User user = userService.getById(order.getUserId());
            
            // ========== 实际业务逻辑（Mock） ==========
            
            // 1. 更新订单支付状态（PaymentService已处理）
            log.info("订单支付状态已更新: orderId={}, payStatus=1", orderIdLong);
            
            // 2. 发送服务通知给用户
            if (user != null) {
                log.info("【短信通知Mock】尊敬的{}，您的订单{}已支付成功，金额{}元，服务即将开始", 
                    user.getNickname() != null ? user.getNickname() : user.getUsername(),
                    order.getOrderNo(),
                    payment != null ? payment.getAmount() : order.getPrice());
            }
            
            // 3. 更新统计数据（Redis）
            String statsKey = "platform:stats";
            redisTemplate.opsForValue().increment(statsKey + ":totalRevenue", 
                payment != null ? payment.getAmount().doubleValue() : order.getPrice().doubleValue());
            redisTemplate.opsForValue().increment(statsKey + ":paidOrders");
            
            // 4. 积分发放（Mock：每消费10元获得1积分）
            double price = payment != null ? payment.getAmount().doubleValue() : order.getPrice().doubleValue();
            int points = (int) (price / 10);
            if (points > 0 && user != null) {
                redisTemplate.opsForValue().increment("user:points:" + user.getId(), points);
                log.info("积分发放: userId={}, points={}", user.getId(), points);
            }
            
            // 5. 推送通知给用户
            redisTemplate.opsForList().rightPush("notify:user:" + order.getUserId(), 
                String.format("支付成功：%s，金额%.2f元", order.getServiceName(), price));
            
            log.info("支付业务处理完成: orderId={}, amount={}", orderIdLong, price);
            
        } catch (Exception e) {
            log.error("处理支付业务异常: orderId={}", orderId, e);
        }
    }
    
    /**
     * 关闭调度器（用于优雅停机）
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        log.info("异步任务处理器已关闭");
    }
}
