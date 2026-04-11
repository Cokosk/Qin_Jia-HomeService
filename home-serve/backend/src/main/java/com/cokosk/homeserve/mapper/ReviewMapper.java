package com.cokosk.homeserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cokosk.homeserve.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 评价Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    
    /**
     * 获取服务者平均评分
     */
    @Select("SELECT AVG(rating) as avgRating, COUNT(*) as totalReview FROM review WHERE worker_id = #{workerId} AND status = 1")
    Map<String, Object> getWorkerRatingStats(Long workerId);
    
    /**
     * 获取服务评价统计
     */
    @Select("SELECT AVG(rating) as avgRating, COUNT(*) as totalReview FROM review WHERE service_id = #{serviceId} AND status = 1")
    Map<String, Object> getServiceRatingStats(Long serviceId);
}
