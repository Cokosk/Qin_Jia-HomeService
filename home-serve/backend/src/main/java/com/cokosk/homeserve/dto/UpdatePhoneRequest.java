package com.cokosk.homeserve.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 更新手机号请求DTO
 */
@Data
public class UpdatePhoneRequest {
    
    private Long userId;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;
}
