package com.cokosk.homeserve.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项目实体类
 */
@Data
@TableName("service_item")
public class ServiceItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String image;
    
    /** 状态: 0-下架 1-上架 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}