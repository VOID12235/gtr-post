package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("certificate")
public class Certificate extends BaseEntity {

    public Long contestId;
    public Long userId;
    public String award;
    public String code;
    public String status;
    public LocalDateTime issuedAt;
}
