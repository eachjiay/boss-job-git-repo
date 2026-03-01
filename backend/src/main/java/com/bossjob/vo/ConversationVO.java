package com.bossjob.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long otherSideUserId;
    private String otherSideUserName; // 真实姓名或用户名
    private String otherSideAvatar;
    private String otherSideRole; // Recruiter 或 Seeker
    private String otherSideCompany; // 如果是招聘者，显示公司名；如果是求职者，显示期望职位等

    private String lastMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    private Integer unreadCount;
}
