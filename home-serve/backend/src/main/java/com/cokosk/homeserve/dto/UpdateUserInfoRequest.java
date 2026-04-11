package com.cokosk.homeserve.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 更新用户信息请求DTO
 */
@Data
public class UpdateUserInfoRequest {
    
    private Long userId;
    
    @Size(max = 30, message = "昵称最长30个字符")
    private String nickname;
    
    private String avatar;
}
