package com.bossjob.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendEmailCodeDTO {

    @NotBlank(message = "邮箱不能为空")
    private String email;
}