package com.btr.platform.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btr.platform.entity.TenantMember;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TenantMemberMapper extends BaseMapper<TenantMember> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM tenant_member WHERE user_id = #{userId} AND status = 'ACTIVE' ORDER BY id LIMIT 1")
    TenantMember selectFirstActiveByUser(@Param("userId") Long userId);
}
