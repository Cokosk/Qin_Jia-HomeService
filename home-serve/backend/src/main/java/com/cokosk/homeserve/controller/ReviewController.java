package com.cokosk.homeserve.controller;

import com.cokosk.homeserve.entity.Review;
import com.cokosk.homeserve.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 创建评价
     */
    @PostMapping("/create")
    public Map<String, Object> createReview(@RequestBody Review review) {
        log.info("创建评价: orderId={}, userId={}, rating={}", 
            review.getOrderId(), review.getUserId(), review.getRating());
        return reviewService.createReview(review);
    }
    
    /**
     * 获取订单评价
     */
    @GetMapping("/order")
    public Map<String, Object> getOrderReview(@RequestParam Long orderId) {
        Map<String, Object> result = new HashMap<>();
        Review review = reviewService.getReviewByOrderId(orderId);
        if (review != null) {
            result.put("code", 200);
            result.put("data", review);
        } else {
            result.put("code", 404);
            result.put("message", "暂无评价");
        }
        return result;
    }
    
    /**
     * 获取订单评价列表（新增接口）
     */
    @GetMapping("/list")
    public Map<String, Object> getReviewList(@RequestParam Long orderId) {
        Map<String, Object> result = new HashMap<>();
        if (orderId == null || orderId <= 0) {
            result.put("code", 400);
            result.put("message", "订单ID无效");
            return result;
        }
        List<Review> reviews = reviewService.getReviewsByOrderId(orderId);
        result.put("code", 200);
        result.put("data", reviews);
        return result;
    }
    
    /**
     * 获取服务者评价列表
     */
    @GetMapping("/worker")
    public Map<String, Object> getWorkerReviews(
            @RequestParam Long workerId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return reviewService.getWorkerReviews(workerId, pageNum, pageSize);
    }
    
    /**
     * 获取服务者评分统计
     */
    @GetMapping("/worker/stats")
    public Map<String, Object> getWorkerRatingStats(@RequestParam Long workerId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", reviewService.getWorkerRatingStats(workerId));
        return result;
    }
    
    /**
     * 获取服务评价列表
     */
    @GetMapping("/service")
    public Map<String, Object> getServiceReviews(
            @RequestParam Long serviceId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return reviewService.getServiceReviews(serviceId, pageNum, pageSize);
    }
    
    /**
     * 获取服务评分统计
     */
    @GetMapping("/service/stats")
    public Map<String, Object> getServiceRatingStats(@RequestParam Long serviceId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", reviewService.getServiceRatingStats(serviceId));
        return result;
    }
    
    /**
     * 商家回复评价
     */
    @PostMapping("/reply")
    public Map<String, Object> replyReview(
            @RequestParam Long reviewId,
            @RequestParam Long workerId,
            @RequestParam String reply) {
        log.info("商家回复评价: reviewId={}, workerId={}", reviewId, workerId);
        return reviewService.replyReview(reviewId, workerId, reply);
    }
}
