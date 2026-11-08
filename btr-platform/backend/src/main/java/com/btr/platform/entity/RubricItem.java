package com.btr.platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("rubric_item")
public class RubricItem extends BaseEntity {

    public Long rubricId;
    public String itemKey;
    public String label;
    public BigDecimal weight;
    public BigDecimal minScore;
    public BigDecimal maxScore;
    public Integer sortOrder;
}
