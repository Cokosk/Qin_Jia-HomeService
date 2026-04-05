package com.cokosk.homeserve.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cokosk.homeserve.entity.Order;
import com.cokosk.homeserve.entity.Review;
import com.cokosk.homeserve.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {
    
    private final StringRedisTemplate redisTemplate;
    private final OrderService orderService;
    
    /**
     * 创建评价
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createReview(Review review) {
        Map<String, Object> result = new HashMap<>();
        
        if (review == null || review.getOrderId() == null || review.getUserId() == null) {
            result.put("code", 400);
            result.put("message", "评价信息无效");
            return result;
        }
        
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            result.put("code", 400);
            result.put("message", "评分必须在1-5之间");
            return result;
        }
        
        Order order = orderService.getById(review.getOrderId());
        if (order == null || order.getStatus() != 3) {
            result.put("code", 400);
            result.put("message", "订单未完成，无法评价");
            return result;
        }
        
        if (!order.getUserId().equals(review.getUserId())) {
            result.put("code", 403);
            result.put("message", "无权限评价该订单");
            return result;
        }
        
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", review.getOrderId());
        if (this.count(queryWrapper) > 0) {
            result.put("code", 400);
            result.put("message", "该订单已评价");
            return result;
        }
        
        review.setWorkerId(order.getWorkerId());
        review.setServiceId(order.getServiceId());
        review.setStatus(1);
        if (review.getAnonymous() == null) review.setAnonymous(0);
        
        boolean saved = this.save(review);
        
        if (saved) {
            clearWorkerRatingCache(review.getWorkerId());
            clearServiceReviewCache(review.getServiceId());
            result.put("code", 200);
            result.put("message", "评价成功");
            result.put("data", review);
        } else {
            result.put("code", 500);
            result.put("message", "评价失败");
        }
        
        return result;
    }
    
    public Review getReviewByOrderId(Long orderId) {
        if (orderId == null) return null;
        return this.getOne(new QueryWrapper<Review>().eq("order_id", orderId));
    }
    
    public Map<String, Object> getWorkerReviews(Long workerId, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        if (workerId == null) {
            result.put("code", 400);
            return result;
        }
        
        Page<Review> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
        Page<Review> pageResult = this.page(page, 
            new QueryWrapper<Review>().eq("worker_id", workerId).eq("status", 1).orderByDesc("create_time"));
        
        result.put("code", 200);
        result.put("data", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return result;
    }
    
    public Map<String, Object> getWorkerRatingStats(Long workerId) {
        Map<String, Object> stats = new HashMap<>();
        if (workerId == null) {
            stats.put("avgRating", 0.0);
            stats.put("totalReview", 0);
            return stats;
        }
        
        Map<String, Object> dbStats = this.baseMapper.getWorkerRatingStats(workerId);
        stats.put("avgRating", dbStats != null ? dbStats.getOrDefault("avgRating", 0.0) : 0.0);
        stats.put("totalReview", dbStats != null ? dbStats.getOrDefault("totalReview", 0) : 0);
        return stats;
    }
    
    public Map<String, Object> getServiceReviews(Long serviceId, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        if (serviceId == null) {
            result.put("code", 400);
            return result;
        }
        
        Page<Review> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
        Page<Review> pageResult = this.page(page,
            new QueryWrapper<Review>().eq("service_id", serviceId).eq("status", 1).orderByDesc("create_time"));
        
        result.put("code", 200);
        result.put("data", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return result;
    }
    
    public Map<String, Object> getServiceRatingStats(Long serviceId) {
        Map<String, Object> stats = new HashMap<>();
        if (serviceId == null) {
            stats.put("avgRating", 0.0);
            stats.put("totalReview", 0);
            return stats;
        }
        
        Map<String, Object> dbStats = this.baseMapper.getServiceRatingStats(serviceId);
        stats.put("avgRating", dbStats != null ? dbStats.getOrDefault("avgRating", 0.0) : 0.0);
        stats.put("totalReview", dbStats != null ? dbStats.getOrDefault("totalReview", 0) : 0);
        return stats;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> replyReview(Long reviewId, Long workerId, String reply) {
        Map<String, Object> result = new HashMap<>();
        
        Review review = this.getById(reviewId);
        if (review == null || !review.getWorkerId().equals(workerId)) {
            result.put("code", 403);
            result.put("message", "无权限回复");
            return result;
        }
        
        review.setReply(reply);
        review.setReplyTime(LocalDateTime.now());
        boolean updated = this.updateById(review);
        
        result.put("code", updated ? 200 : 500);
        result.put("message", updated ? "回复成功" : "回复失败");
        return result;
    }
    
    // ========== 新增接口方法 ==========
    
    /**
     * 获取工人平均评分
     */
    public Double getAvgRatingByWorkerId(Long workerId) {
        if (workerId == null) return null;
        
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("worker_id", workerId).eq("status", 1)
            .select("AVG(rating) as avgRating");
        
        Map<String, Object> result = this.getMap(queryWrapper);
        if (result != null && result.get("avgRating") != null) {
            Object avg = result.get("avgRating");
            if (avg instanceof Number) return ((Number) avg).doubleValue();
            return Double.parseDouble(avg.toString());
        }
        return null;
    }
    
    /**
     * 获取订单评价列表
     */
    public List<Review> getReviewsByOrderId(Long orderId) {
        if (orderId == null) return List.of();
        return this.lambdaQuery().eq(Review::getOrderId, orderId).eq(Review::getStatus, 1).list();
    }
    
    private void clearWorkerRatingCache(Long workerId) {
        if (workerId != null) redisTemplate.delete("worker:rating:" + workerId);
    }
    
    private void clearServiceReviewCache(Long serviceId) {
        if (serviceId != null) redisTemplate.delete("service:review:" + serviceId);
    }
    
    /**
     * 管理员隐藏/显示评价
     */
    public Map<String, Object> toggleReviewStatus(Long reviewId, Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        if (reviewId == null || status == null || (status != 0 && status != 1)) {
            result.put("code", 400);
            result.put("message", "参数无效");
            return result;
        }
        
        Review review = this.getById(reviewId);
        if (review == null) {
            result.put("code", 404);
            result.put("message", "评价不存在");
            return result;
        }
        
        review.setStatus(status);
        boolean updated = this.updateById(review);
        
        clearWorkerRatingCache(review.getWorkerId());
        clearServiceReviewCache(review.getServiceId());
        
        result.put("code", updated ? 200 : 500);
        result.put("message", updated ? "状态更新成功" : "状态更新失败");
        return result;
    }
}
