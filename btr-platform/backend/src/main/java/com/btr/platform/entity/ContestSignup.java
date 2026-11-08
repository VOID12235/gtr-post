package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("contest_signup")
public class ContestSignup extends BaseEntity {

    public Long contestId;
    public Long userId;
    public String teamName;
    public String contact;
    public String status;
}
