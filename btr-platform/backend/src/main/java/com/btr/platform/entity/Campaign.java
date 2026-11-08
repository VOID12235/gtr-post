package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("campaign")
public class Campaign {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long sponsorId;
    public Long tenantId;
    public String name;
    public String type;
    public BigDecimal budget;
    public String status;
    public String creativeText;
    public String creativeUrl;
    public LocalDateTime startAt;
    public LocalDateTime endAt;
    public LocalDateTime createdAt;
}
