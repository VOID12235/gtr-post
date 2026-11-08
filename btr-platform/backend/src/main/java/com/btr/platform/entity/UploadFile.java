package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("upload_file")
public class UploadFile {

    @TableId(type = IdType.AUTO)
    public Long id;
    public Long tenantId;
    public String originalName;
    public String storedName;
    public String contentType;
    public Long size;
    public String url;
    public LocalDateTime createdAt;
}
