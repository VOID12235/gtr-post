package com.btr.platform.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btr.platform.entity.UploadFile;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UploadFileMapper extends BaseMapper<UploadFile> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM upload_file WHERE id = #{id}")
    UploadFile selectByIdIgnoreTenant(@Param("id") Long id);
}
