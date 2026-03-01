package com.bossjob.vo;

import lombok.Data;

/**
 * 用户信息VO
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String realName;
    private String avatar;
    private String role;
    private Integer status;

    // 如果是HR，关联公司信息
    private Long companyId;
    private String companyName;
    private String companyLogo;
}
