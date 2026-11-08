package com.btr.platform.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btr.platform.entity.Certificate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CertificateMapper extends BaseMapper<Certificate> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM certificate WHERE code = #{code}")
    Certificate selectByCodeIgnoreTenant(@Param("code") String code);
}
