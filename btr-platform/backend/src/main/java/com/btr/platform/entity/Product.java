package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long tenantId;
    public String name;
    public String description;
    public String cover;
    public BigDecimal price;
    public Integer stock;
    public String status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
