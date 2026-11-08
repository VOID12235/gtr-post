package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("prize_pool")
public class PrizePool {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long tenantId;
    public Long contestId;
    public BigDecimal totalAmount;
    public BigDecimal fundedAmount;
    public String status;
    public String note;
    public LocalDateTime updatedAt;
}
