package com.cokosk.homeserve.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Data
@TableName("payment")
public class Payment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 支付流水号 */
    private String paymentNo;
    
    /** 订单ID */
    private Long orderId;
    
    /** 用户ID */
    private Long userId;
    
    /** 支付金额 */
    private BigDecimal amount;
    
    /** 支付方式 1-微信 2-支付宝 3-余额 */
    private Integer payMethod;
    
    /** 支付状态 0-待支付 1-已支付 2-已退款 */
    private Integer status;
    
    /** 第三方支付流水号 */
    private String transactionId;
    
    /** 支付时间 */
    private LocalDateTime payTime;
    
    /** 退款时间 */
    private LocalDateTime refundTime;
    
    /** 退款金额 */
    private BigDecimal refundAmount;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
