package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.InsurancePolicy;
import com.btr.platform.entity.PrizePool;
import com.btr.platform.entity.SafetyPlan;
import com.btr.platform.mapper.InsurancePolicyMapper;
import com.btr.platform.mapper.PrizePoolMapper;
import com.btr.platform.mapper.SafetyPlanMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RiskService {

    private final PrizePoolMapper prizePoolMapper;
    private final InsurancePolicyMapper insuranceMapper;
    private final SafetyPlanMapper safetyMapper;

    public RiskService(PrizePoolMapper prizePoolMapper,
                       InsurancePolicyMapper insuranceMapper,
                       SafetyPlanMapper safetyMapper) {
        this.prizePoolMapper = prizePoolMapper;
        this.insuranceMapper = insuranceMapper;
        this.safetyMapper = safetyMapper;
    }

    public PrizePool prize(Long contestId) {
        return prizePoolMapper.selectOne(new QueryWrapper<PrizePool>().eq("contest_id", contestId));
    }

    public List<InsurancePolicy> insurances(Long contestId) {
        return insuranceMapper.selectList(new QueryWrapper<InsurancePolicy>()
                .eq("contest_id", contestId)
                .orderByDesc("id"));
    }

    public SafetyPlan safety(Long contestId) {
        return safetyMapper.selectOne(new QueryWrapper<SafetyPlan>().eq("contest_id", contestId));
    }

    @Transactional
    public PrizePool savePrize(Long contestId, P1Dtos.PrizeRequest request) {
        PrizePool pool = prize(contestId);
        if (pool == null) {
            pool = new PrizePool();
            pool.tenantId = TenantContext.require();
            pool.contestId = contestId;
        }
        pool.totalAmount = request.totalAmount();
        pool.fundedAmount = request.fundedAmount() == null ? java.math.BigDecimal.ZERO : request.fundedAmount();
        pool.status = request.status();
        pool.note = request.note();
        pool.updatedAt = LocalDateTime.now();
        if (pool.id == null) {
            prizePoolMapper.insert(pool);
        } else {
            prizePoolMapper.updateById(pool);
        }
        return pool;
    }

    @Transactional
    public InsurancePolicy saveInsurance(Long contestId, P1Dtos.InsuranceRequest request) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.tenantId = TenantContext.require();
        policy.contestId = contestId;
        policy.type = request.type();
        policy.insurer = request.insurer();
        policy.policyNo = request.policyNo();
        policy.coverage = request.coverage();
        policy.validFrom = request.validFrom();
        policy.validTo = request.validTo();
        policy.status = request.status();
        policy.createdAt = LocalDateTime.now();
        insuranceMapper.insert(policy);
        return policy;
    }

    @Transactional
    public SafetyPlan saveSafety(Long contestId, P1Dtos.SafetyRequest request) {
        SafetyPlan plan = safety(contestId);
        if (plan == null) {
            plan = new SafetyPlan();
            plan.tenantId = TenantContext.require();
            plan.contestId = contestId;
        }
        plan.level = request.level();
        plan.venue = request.venue();
        plan.capacity = request.capacity();
        plan.plan = request.plan();
        plan.status = request.status();
        if (plan.id == null) {
            plan.createdAt = LocalDateTime.now();
            safetyMapper.insert(plan);
        } else {
            safetyMapper.updateById(plan);
        }
        return plan;
    }

    public void requireContest(Long contestId) {
        if (contestId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "缺少赛事");
        }
    }
}
