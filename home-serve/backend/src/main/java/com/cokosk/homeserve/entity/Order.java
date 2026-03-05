package com.cokosk.homeserve.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("orders")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 订单号 */
    private String orderNo;
    
    private Long userId;
    private Long workerId;
    private Long serviceId;
    private String serviceName;
    private BigDecimal price;
    
    /** 预约时间 */
    private LocalDateTime appointmentTime;
    private String address;
    private String phone;
    private String remark;
    
    /** 状态: 0-待抢单 1-已接单 2-服务中 3-已完成 4-已取消 */
    private Integer status;
    
    private LocalDateTime grabTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private LocalDateTime payTime;
    
    /** 支付状态: 0-未支付 1-已支付 */
    private Integer payStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}