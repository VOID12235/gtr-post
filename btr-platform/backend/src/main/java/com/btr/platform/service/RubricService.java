package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Rubric;
import com.btr.platform.entity.RubricItem;
import com.btr.platform.mapper.RubricItemMapper;
import com.btr.platform.mapper.RubricMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RubricService {

    private final RubricMapper rubricMapper;
    private final RubricItemMapper itemMapper;

    public RubricService(RubricMapper rubricMapper, RubricItemMapper itemMapper) {
        this.rubricMapper = rubricMapper;
        this.itemMapper = itemMapper;
    }

    @Transactional
    public P1Dtos.RubricView create(P1Dtos.CreateRubricRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "评分表至少需要一个维度");
        }
        BigDecimal sum = request.items().stream()
                .map(P1Dtos.RubricItemRequest::weight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("0.001")) > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "评分维度权重之和必须为 1");
        }

        Rubric rubric = new Rubric();
        rubric.tenantId = TenantContext.require();
        rubric.contestId = request.contestId();
        rubric.name = request.name();
        rubric.mode = request.mode();
        rubric.status = "ACTIVE";
        rubricMapper.insert(rubric);

        int sort = 0;
        for (P1Dtos.RubricItemRequest itemRequest : request.items()) {
            RubricItem item = new RubricItem();
            item.tenantId = TenantContext.require();
            item.rubricId = rubric.id;
            item.itemKey = itemRequest.key();
            item.label = itemRequest.label();
            item.weight = itemRequest.weight();
            item.minScore = itemRequest.min() == null ? BigDecimal.ZERO : itemRequest.min();
            item.maxScore = itemRequest.max() == null ? new BigDecimal("100") : itemRequest.max();
            item.sortOrder = sort++;
            itemMapper.insert(item);
        }
        return toView(rubric);
    }

    public List<P1Dtos.RubricView> listByContest(Long contestId) {
        return rubricMapper.selectList(new QueryWrapper<Rubric>()
                        .eq("contest_id", contestId)
                        .orderByDesc("id"))
                .stream()
                .map(this::toView)
                .toList();
    }

    public P1Dtos.RubricView toView(Rubric rubric) {
        List<P1Dtos.RubricItemView> items = itemMapper.selectList(new QueryWrapper<RubricItem>()
                        .eq("rubric_id", rubric.id)
                        .orderByAsc("sort_order"))
                .stream()
                .map(item -> new P1Dtos.RubricItemView(
                        item.id,
                        item.itemKey,
                        item.label,
                        item.weight,
                        item.minScore,
                        item.maxScore,
                        item.sortOrder
                ))
                .toList();
        return new P1Dtos.RubricView(
                rubric.id,
                rubric.contestId,
                rubric.name,
                rubric.mode,
                rubric.status,
                items
        );
    }
}
