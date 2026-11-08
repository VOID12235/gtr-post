package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("safety_plan")
public class SafetyPlan {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long tenantId;
    public Long contestId;
    public String level;
    public String venue;
    public Integer capacity;
    public String plan;
    public String status;
    public LocalDateTime createdAt;
}
