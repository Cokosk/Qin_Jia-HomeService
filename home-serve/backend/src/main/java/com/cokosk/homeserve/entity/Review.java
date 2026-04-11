package com.cokosk.homeserve.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@Data
@TableName("review")
public class Review {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 订单ID */
    private Long orderId;
    
    /** 用户ID */
    private Long userId;
    
    /** 服务者ID */
    private Long workerId;
    
    /** 服务ID */
    private Long serviceId;
    
    /** 评分 1-5分 */
    private Integer rating;
    
    /** 评价内容 */
    private String content;
    
    /** 评价图片，逗号分隔 */
    private String images;
    
    /** 是否匿名 0-否 1-是 */
    private Integer anonymous;
    
    /** 状态 0-隐藏 1-显示 */
    private Integer status;
    
    /** 商家回复 */
    private String reply;
    
    /** 回复时间 */
    private LocalDateTime replyTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
