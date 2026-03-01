package com.bossjob.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginDTO {

    @NotBlank(message = "微信授权码不能为空")
    private String code;
}
