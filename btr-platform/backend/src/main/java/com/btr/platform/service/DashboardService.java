package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Certificate;
import com.btr.platform.entity.Contest;
import com.btr.platform.entity.ContestSignup;
import com.btr.platform.entity.ContestWork;
import com.btr.platform.entity.Notification;
import com.btr.platform.mapper.CertificateMapper;
import com.btr.platform.mapper.ContestMapper;
import com.btr.platform.mapper.ContestSignupMapper;
import com.btr.platform.mapper.ContestWorkMapper;
import com.btr.platform.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ContestMapper contestMapper;
    private final ContestSignupMapper signupMapper;
    private final ContestWorkMapper workMapper;
    private final CertificateMapper certificateMapper;
    private final NotificationMapper notificationMapper;

    public DashboardService(ContestMapper contestMapper,
                            ContestSignupMapper signupMapper,
                            ContestWorkMapper workMapper,
                            CertificateMapper certificateMapper,
                            NotificationMapper notificationMapper) {
        this.contestMapper = contestMapper;
        this.signupMapper = signupMapper;
        this.workMapper = workMapper;
        this.certificateMapper = certificateMapper;
        this.notificationMapper = notificationMapper;
    }

    public P1Dtos.DashboardView view(Long userId) {
        long contests = count(contestMapper.selectCount(new QueryWrapper<Contest>()));
        long signups = count(signupMapper.selectCount(new QueryWrapper<ContestSignup>()));
        long works = count(workMapper.selectCount(new QueryWrapper<ContestWork>()));
        long certificates = count(certificateMapper.selectCount(new QueryWrapper<Certificate>()));
        long unread = count(notificationMapper.selectCount(new QueryWrapper<Notification>()
                .eq("user_id", userId)
                .isNull("read_at")));
        return new P1Dtos.DashboardView(contests, signups, works, certificates, unread);
    }

    private long count(Long value) {
        return value == null ? 0 : value;
    }
}
