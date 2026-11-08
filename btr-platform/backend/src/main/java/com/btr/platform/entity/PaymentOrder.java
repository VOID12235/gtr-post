package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("payment_order")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long tenantId;
    public Long contestId;
    public Long userId;
    public BigDecimal amount;
    public String status;
    public String method;
    public String reference;
    public LocalDateTime createdAt;
    public LocalDateTime paidAt;
}
