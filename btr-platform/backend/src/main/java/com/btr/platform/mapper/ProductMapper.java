package com.btr.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btr.platform.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND tenant_id = #{tenantId} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("tenantId") Long tenantId, @Param("quantity") int quantity);

    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id} AND tenant_id = #{tenantId}")
    int restoreStock(@Param("id") Long id, @Param("tenantId") Long tenantId, @Param("quantity") int quantity);
}
