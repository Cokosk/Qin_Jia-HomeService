package com.cokosk.homeserve.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String password;
    private String phone;
    private String nickname;
    private String avatar;
    
    /** 角色: 0-用户 1-服务者 2-管理员 */
    private Integer role;
    
    /** 状态: 0-禁用 1-正常 */
    private Integer status;
    
    /** 信用分 */
    private Integer creditScore;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}