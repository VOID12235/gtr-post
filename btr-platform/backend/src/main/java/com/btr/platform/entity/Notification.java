package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("notification")
public class Notification extends BaseEntity {

    public Long userId;
    public String type;
    public String title;
    public String body;
    public String link;
    public LocalDateTime readAt;
}
