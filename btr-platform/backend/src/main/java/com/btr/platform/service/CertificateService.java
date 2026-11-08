package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Certificate;
import com.btr.platform.entity.Contest;
import com.btr.platform.entity.ContestWork;
import com.btr.platform.entity.SysUser;
import com.btr.platform.mapper.CertificateMapper;
import com.btr.platform.mapper.ContestMapper;
import com.btr.platform.mapper.ContestWorkMapper;
import com.btr.platform.mapper.SysUserMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CertificateService {

    private final CertificateMapper certificateMapper;
    private final ContestMapper contestMapper;
    private final ContestWorkMapper workMapper;
    private final SysUserMapper userMapper;

    public CertificateService(CertificateMapper certificateMapper,
                              ContestMapper contestMapper,
                              ContestWorkMapper workMapper,
                              SysUserMapper userMapper) {
        this.certificateMapper = certificateMapper;
        this.contestMapper = contestMapper;
        this.workMapper = workMapper;
        this.userMapper = userMapper;
    }

    public List<P1Dtos.CertificateView> listByContest(Long contestId) {
        return certificateMapper.selectList(new QueryWrapper<Certificate>()
                        .eq("contest_id", contestId)
                        .orderByDesc("id"))
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public List<P1Dtos.CertificateView> generate(P1Dtos.CertificateRequest request) {
        Contest contest = contestMapper.selectById(request.contestId());
        if (contest == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "赛事不存在");
        }
        String award = request.award() == null || request.award().isBlank() ? "参赛证书" : request.award();
        List<ContestWork> works = workMapper.selectList(new QueryWrapper<ContestWork>()
                .eq("contest_id", request.contestId())
                .isNotNull("total_score"));
        for (ContestWork work : works) {
            Long exists = certificateMapper.selectCount(new QueryWrapper<Certificate>()
                    .eq("contest_id", request.contestId())
                    .eq("user_id", work.userId));
            if (exists != null && exists > 0) {
                continue;
            }
            Certificate certificate = new Certificate();
            certificate.tenantId = TenantContext.require();
            certificate.contestId = request.contestId();
            certificate.userId = work.userId;
            certificate.award = award;
            certificate.code = "BTR-" + TenantContext.require() + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            certificate.status = "ISSUED";
            certificate.issuedAt = LocalDateTime.now();
            certificateMapper.insert(certificate);
        }
        return listByContest(request.contestId());
    }

    public P1Dtos.CertificateView verify(String code) {
        Certificate certificate = certificateMapper.selectByCodeIgnoreTenant(code);
        if (certificate == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "证书不存在");
        }
        Long previous = TenantContext.get();
        TenantContext.set(certificate.tenantId);
        try {
            return toView(certificate);
        } finally {
            if (previous == null) {
                TenantContext.clear();
            } else {
                TenantContext.set(previous);
            }
        }
    }

    public P1Dtos.CertificateView toView(Certificate certificate) {
        Contest contest = contestMapper.selectById(certificate.contestId);
        SysUser holder = certificate.userId == null ? null : userMapper.selectById(certificate.userId);
        return new P1Dtos.CertificateView(
                certificate.id,
                certificate.contestId,
                contest == null ? null : contest.title,
                certificate.award,
                certificate.code,
                certificate.status,
                certificate.issuedAt,
                holder == null ? null : holder.getNickname()
        );
    }
}
