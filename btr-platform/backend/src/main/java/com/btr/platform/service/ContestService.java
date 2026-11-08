package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Contest;
import com.btr.platform.mapper.ContestMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ContestService {

    private static final Set<String> STATUSES = Set.of(
            "DRAFT", "SIGNUP", "SUBMISSION", "JUDGING", "PUBLISHED", "FINISHED"
    );

    private final ContestMapper contestMapper;

    public ContestService(ContestMapper contestMapper) {
        this.contestMapper = contestMapper;
    }

    public List<Contest> list() {
        return contestMapper.selectList(new QueryWrapper<Contest>().orderByDesc("id"));
    }

    public Contest get(Long id) {
        Contest contest = contestMapper.selectById(id);
        if (contest == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "赛事不存在");
        }
        return contest;
    }

    @Transactional
    public Contest create(P1Dtos.CreateContestRequest request) {
        Contest contest = new Contest();
        contest.tenantId = TenantContext.require();
        contest.title = request.title();
        contest.summary = request.summary();
        contest.cover = request.cover();
        contest.mode = request.mode() == null ? "ONLINE" : request.mode();
        contest.status = "DRAFT";
        contest.feeEnabled = Boolean.TRUE.equals(request.feeEnabled());
        contest.feeAmount = request.feeAmount() == null ? BigDecimal.ZERO : request.feeAmount();
        contest.signupStart = request.signupStart();
        contest.signupEnd = request.signupEnd();
        contest.submitStart = request.submitStart();
        contest.submitEnd = request.submitEnd();
        contest.reviewStart = request.reviewStart();
        contest.reviewEnd = request.reviewEnd();
        contestMapper.insert(contest);
        return contest;
    }

    @Transactional
    public Contest updateStatus(Long id, String status) {
        if (!STATUSES.contains(status)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "未知赛事状态");
        }
        Contest contest = get(id);
        contest.status = status;
        if ("PUBLISHED".equals(status) && contest.publishedAt == null) {
            contest.publishedAt = LocalDateTime.now();
        }
        contestMapper.updateById(contest);
        return contest;
    }

    public List<Contest> publicList() {
        return contestMapper.selectList(new QueryWrapper<Contest>()
                .ne("status", "DRAFT")
                .orderByDesc("id"));
    }

    public P1Dtos.ContestView toView(Contest contest) {
        return new P1Dtos.ContestView(
                contest.id,
                contest.title,
                contest.summary,
                contest.cover,
                contest.mode,
                contest.status,
                contest.feeEnabled,
                contest.feeAmount,
                contest.signupStart,
                contest.signupEnd,
                contest.submitStart,
                contest.submitEnd,
                contest.reviewStart,
                contest.reviewEnd,
                contest.publishedAt
        );
    }
}
