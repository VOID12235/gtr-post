package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("contest")
public class Contest extends BaseEntity {

    public String title;
    public String summary;
    public String cover;
    public String mode;
    public String status;
    public Boolean feeEnabled;
    public BigDecimal feeAmount;
    public LocalDateTime signupStart;
    public LocalDateTime signupEnd;
    public LocalDateTime submitStart;
    public LocalDateTime submitEnd;
    public LocalDateTime reviewStart;
    public LocalDateTime reviewEnd;
    public LocalDateTime publishedAt;
}
