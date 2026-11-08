package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("insurance_policy")
public class InsurancePolicy {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long tenantId;
    public Long contestId;
    public String type;
    public String insurer;
    public String policyNo;
    public String coverage;
    public LocalDateTime validFrom;
    public LocalDateTime validTo;
    public String status;
    public LocalDateTime createdAt;
}
