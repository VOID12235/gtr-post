package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("ad_event")
public class AdEvent {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long campaignId;
    public Long placementId;
    public String eventType;
    public Long userId;
    public String ip;
    public String userAgent;
    public LocalDateTime occurredAt;
}
