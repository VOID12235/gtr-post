package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Contest;
import com.btr.platform.entity.ContestWork;
import com.btr.platform.entity.JudgeScore;
import com.btr.platform.mapper.ContestMapper;
import com.btr.platform.mapper.ContestWorkMapper;
import com.btr.platform.mapper.JudgeScoreMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JudgeService {

    private final ContestMapper contestMapper;
    private final ContestWorkMapper workMapper;
    private final JudgeScoreMapper scoreMapper;
    private final WorkService workService;

    public JudgeService(ContestMapper contestMapper,
                        ContestWorkMapper workMapper,
                        JudgeScoreMapper scoreMapper,
                        WorkService workService) {
        this.contestMapper = contestMapper;
        this.workMapper = workMapper;
        this.scoreMapper = scoreMapper;
        this.workService = workService;
    }

    public List<P1Dtos.WorkView> tasks(Long contestId, Long judgeId) {
        Set<Long> scored = scoreMapper.selectList(new QueryWrapper<JudgeScore>()
                        .eq("contest_id", contestId)
                        .eq("judge_id", judgeId))
                .stream()
                .map(score -> score.workId)
                .collect(Collectors.toSet());

        return workMapper.selectList(new QueryWrapper<ContestWork>()
                        .eq("contest_id", contestId)
                        .orderByAsc("id"))
                .stream()
                .filter(work -> !scored.contains(work.id))
                .map(workService::toView)
                .toList();
    }

    @Transactional
    public JudgeScore score(P1Dtos.ScoreRequest request, Long judgeId) {
        ContestWork work = workMapper.selectById(request.workId());
        if (work == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "作品不存在");
        }
        Contest contest = contestMapper.selectById(work.contestId);
        if (contest == null || !"JUDGING".equals(contest.status)) {
            throw new BizException(ErrorCode.CONFLICT, "当前阶段不开放打分");
        }
        if (request.totalScore().compareTo(BigDecimal.ZERO) < 0
                || request.totalScore().compareTo(new BigDecimal("100")) > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "分数必须在 0 到 100 之间");
        }

        JudgeScore score = scoreMapper.selectOne(new QueryWrapper<JudgeScore>()
                .eq("work_id", request.workId())
                .eq("judge_id", judgeId));
        if (score == null) {
            score = new JudgeScore();
            score.tenantId = TenantContext.require();
            score.contestId = work.contestId;
            score.workId = work.id;
            score.judgeId = judgeId;
            score.totalScore = request.totalScore();
            score.comment = request.comment();
            scoreMapper.insert(score);
        } else {
            score.totalScore = request.totalScore();
            score.comment = request.comment();
            scoreMapper.updateById(score);
        }

        List<JudgeScore> scores = scoreMapper.selectList(new QueryWrapper<JudgeScore>()
                .eq("work_id", work.id));
        BigDecimal sum = scores.stream().map(s -> s.totalScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        work.totalScore = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        workMapper.updateById(work);
        return score;
    }

    public List<JudgeScore> scores(Long contestId) {
        return scoreMapper.selectList(new QueryWrapper<JudgeScore>()
                .eq("contest_id", contestId)
                .orderByDesc("id"));
    }
}
