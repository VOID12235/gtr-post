package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("sponsor")
public class Sponsor {

    @TableId(type = IdType.AUTO)
    public Long id;
    public String name;
    public String industry;
    public String contactName;
    public String contactEmail;
    public String status;
    public LocalDateTime createdAt;
}
