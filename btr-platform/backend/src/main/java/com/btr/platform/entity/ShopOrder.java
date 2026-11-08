package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("shop_order")
public class ShopOrder {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long tenantId;
    public Long productId;
    public Long userId;
    public Integer quantity;
    public BigDecimal amount;
    public String status;
    public String contactName;
    public String contactPhone;
    public String address;
    public String note;
    public String trackingNo;
    public LocalDateTime createdAt;
    public LocalDateTime paidAt;
    public LocalDateTime shippedAt;
}
