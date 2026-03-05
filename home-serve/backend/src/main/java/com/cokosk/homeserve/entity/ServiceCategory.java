package com.cokosk.homeserve.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务分类实体类
 */
@Data
@TableName("service_category")
public class ServiceCategory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String icon;
    private Integer sort;
    
    /** 状态: 0-禁用 1-正常 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}