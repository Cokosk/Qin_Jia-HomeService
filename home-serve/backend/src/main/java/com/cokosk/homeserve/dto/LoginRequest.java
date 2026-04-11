package com.cokosk.homeserve.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 支持用户端和工人端登录区分
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 登录类型: "user" - 用户端登录, "worker" - 工人端登录
     * 默认为 "user"
     */
    private String loginType = "user";
}
