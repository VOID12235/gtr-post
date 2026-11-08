package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long tenantId;
    public String configGroup;
    public String configKey;
    public String configValue;
    public String description;
    public LocalDateTime updatedAt;
}
