package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("judge_score")
public class JudgeScore extends BaseEntity {

    public Long contestId;
    public Long workId;
    public Long judgeId;
    public BigDecimal totalScore;
    public String comment;
}
