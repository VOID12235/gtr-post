package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("campaign_placement")
public class CampaignPlacement {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long campaignId;
    public String slot;
    public Long contestId;
    public Integer weight;
    public LocalDateTime startAt;
    public LocalDateTime endAt;
}
