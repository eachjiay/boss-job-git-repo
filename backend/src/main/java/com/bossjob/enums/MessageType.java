package com.bossjob.enums;

/**
 * 消息类型枚举
 * 1 = 文本消息 (包括图片等附件)
 * 2 = 简历申请/投递通知
 * 3 = 系统提示消息
 */
public enum MessageType {
    TEXT(1, "文本消息"),
    RESUME_REQUEST(2, "简历申请"),
    SYSTEM(3, "系统提示"),
    CALL_INVITE(4, "发起通话"),
    CALL_ACCEPT(5, "接受通话"),
    CALL_REJECT(6, "拒绝通话/结束通话"),
    CALL_SIGNALING(7, "WebRTC信令");

    private final Integer code;
    private final String description;

    MessageType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MessageType fromCode(Integer code) {
        if (code == null)
            return TEXT;
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return TEXT;
    }
}
