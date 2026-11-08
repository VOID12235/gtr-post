package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("contest_work")
public class ContestWork extends BaseEntity {

    public Long contestId;
    public Long signupId;
    public Long userId;
    public String title;
    public String description;
    public String videoUrl;
    public String fileUrl;
    public String status;
    public BigDecimal totalScore;
}
