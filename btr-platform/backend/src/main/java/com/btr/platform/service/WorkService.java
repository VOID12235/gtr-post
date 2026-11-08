package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Contest;
import com.btr.platform.entity.ContestSignup;
import com.btr.platform.entity.ContestWork;
import com.btr.platform.entity.SysUser;
import com.btr.platform.mapper.ContestMapper;
import com.btr.platform.mapper.ContestSignupMapper;
import com.btr.platform.mapper.ContestWorkMapper;
import com.btr.platform.mapper.SysUserMapper;
import com.btr.platform.security.UserPrincipal;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class WorkService {

    private static final Set<String> PUBLIC_STATUSES = Set.of("SUBMISSION", "JUDGING", "PUBLISHED", "FINISHED");

    private final ContestMapper contestMapper;
    private final ContestSignupMapper signupMapper;
    private final ContestWorkMapper workMapper;
    private final SysUserMapper userMapper;

    public WorkService(ContestMapper contestMapper,
                       ContestSignupMapper signupMapper,
                       ContestWorkMapper workMapper,
                       SysUserMapper userMapper) {
        this.contestMapper = contestMapper;
        this.signupMapper = signupMapper;
        this.workMapper = workMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public ContestWork upsert(P1Dtos.WorkRequest request, UserPrincipal principal) {
        Contest contest = contestMapper.selectById(request.contestId());
        if (contest == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "赛事不存在");
        }
        if (!"SUBMISSION".equals(contest.status)) {
            throw new BizException(ErrorCode.CONFLICT, "当前阶段不开放作品提交");
        }

        ContestSignup signup = signupMapper.selectOne(new QueryWrapper<ContestSignup>()
                .eq("contest_id", request.contestId())
                .eq("user_id", principal.userId()));
        if (signup == null || !"APPROVED".equals(signup.status)) {
            throw new BizException(ErrorCode.FORBIDDEN, "请先完成报名");
        }

        ContestWork work = workMapper.selectOne(new QueryWrapper<ContestWork>()
                .eq("signup_id", signup.id));
        if (work == null) {
            work = new ContestWork();
            work.tenantId = TenantContext.require();
            work.contestId = request.contestId();
            work.signupId = signup.id;
            work.userId = principal.userId();
            work.status = "SUBMITTED";
            work.title = request.title();
            work.description = request.description();
            work.videoUrl = request.videoUrl();
            work.fileUrl = request.fileUrl();
            workMapper.insert(work);
        } else {
            work.title = request.title();
            work.description = request.description();
            work.videoUrl = request.videoUrl();
            work.fileUrl = request.fileUrl();
            work.status = "SUBMITTED";
            workMapper.updateById(work);
        }
        return work;
    }

    public List<ContestWork> mine(Long userId) {
        return workMapper.selectList(new QueryWrapper<ContestWork>()
                .eq("user_id", userId)
                .orderByDesc("id"));
    }

    public List<ContestWork> listByContest(Long contestId) {
        return workMapper.selectList(new QueryWrapper<ContestWork>()
                .eq("contest_id", contestId)
                .orderByDesc("id"));
    }

    public List<P1Dtos.WorkView> publicList(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null || !PUBLIC_STATUSES.contains(contest.status)) {
            return List.of();
        }
        return listByContest(contestId).stream().map(this::toView).toList();
    }

    public P1Dtos.WorkView toView(ContestWork work) {
        SysUser author = userMapper.selectById(work.userId);
        return new P1Dtos.WorkView(
                work.id,
                work.contestId,
                work.title,
                work.description,
                work.videoUrl,
                work.fileUrl,
                work.status,
                work.totalScore,
                author == null ? null : author.getNickname()
        );
    }
}
