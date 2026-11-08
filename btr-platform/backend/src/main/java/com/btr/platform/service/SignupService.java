package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Contest;
import com.btr.platform.entity.ContestSignup;
import com.btr.platform.mapper.ContestMapper;
import com.btr.platform.mapper.ContestSignupMapper;
import com.btr.platform.security.UserPrincipal;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SignupService {

    private final ContestMapper contestMapper;
    private final ContestSignupMapper signupMapper;
    private final PaymentService paymentService;

    public SignupService(ContestMapper contestMapper, ContestSignupMapper signupMapper, PaymentService paymentService) {
        this.contestMapper = contestMapper;
        this.signupMapper = signupMapper;
        this.paymentService = paymentService;
    }

    @Transactional
    public ContestSignup signup(P1Dtos.SignupRequest request, UserPrincipal principal) {
        Contest contest = contestMapper.selectById(request.contestId());
        if (contest == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "赛事不存在");
        }
        if (!"SIGNUP".equals(contest.status) && !"SUBMISSION".equals(contest.status)) {
            throw new BizException(ErrorCode.CONFLICT, "当前阶段不开放报名");
        }

        Long exists = signupMapper.selectCount(new QueryWrapper<ContestSignup>()
                .eq("contest_id", request.contestId())
                .eq("user_id", principal.userId()));
        if (exists != null && exists > 0) {
            throw new BizException(ErrorCode.CONFLICT, "你已报名该赛事");
        }

        ContestSignup signup = new ContestSignup();
        signup.tenantId = TenantContext.require();
        signup.contestId = request.contestId();
        signup.userId = principal.userId();
        signup.teamName = request.teamName();
        signup.contact = request.contact();
        boolean feeRequired = Boolean.TRUE.equals(contest.feeEnabled)
                && contest.feeAmount != null
                && contest.feeAmount.compareTo(java.math.BigDecimal.ZERO) > 0;
        signup.status = feeRequired ? "PENDING_PAYMENT" : "APPROVED";
        signupMapper.insert(signup);
        if (feeRequired) {
            paymentService.createForContest(contest, principal);
        }
        return signup;
    }

    public List<ContestSignup> mine(Long userId) {
        return signupMapper.selectList(new QueryWrapper<ContestSignup>()
                .eq("user_id", userId)
                .orderByDesc("id"));
    }

    public List<ContestSignup> listByContest(Long contestId) {
        return signupMapper.selectList(new QueryWrapper<ContestSignup>()
                .eq("contest_id", contestId)
                .orderByDesc("id"));
    }
}
