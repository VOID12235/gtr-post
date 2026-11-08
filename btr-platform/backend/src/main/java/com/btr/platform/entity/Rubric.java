package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("rubric")
public class Rubric extends BaseEntity {

    public Long contestId;
    public String name;
    public String mode;
    public String status;
}
