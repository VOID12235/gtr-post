package com.btr.platform.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.entity.Campaign;
import com.btr.platform.entity.CampaignPlacement;
import com.btr.platform.entity.Certificate;
import com.btr.platform.entity.Contest;
import com.btr.platform.entity.ContestSignup;
import com.btr.platform.entity.ContestWork;
import com.btr.platform.entity.Notification;
import com.btr.platform.entity.Rubric;
import com.btr.platform.entity.RubricItem;
import com.btr.platform.entity.Sponsor;
import com.btr.platform.entity.SysUser;
import com.btr.platform.entity.Tenant;
import com.btr.platform.entity.TenantMember;
import com.btr.platform.entity.JudgeScore;
import com.btr.platform.mapper.CampaignMapper;
import com.btr.platform.mapper.CampaignPlacementMapper;
import com.btr.platform.mapper.CertificateMapper;
import com.btr.platform.mapper.ContestMapper;
import com.btr.platform.mapper.ContestSignupMapper;
import com.btr.platform.mapper.ContestWorkMapper;
import com.btr.platform.mapper.JudgeScoreMapper;
import com.btr.platform.mapper.NotificationMapper;
import com.btr.platform.mapper.RubricItemMapper;
import com.btr.platform.mapper.RubricMapper;
import com.btr.platform.mapper.SponsorMapper;
import com.btr.platform.mapper.SysUserMapper;
import com.btr.platform.mapper.TenantMapper;
import com.btr.platform.mapper.TenantMemberMapper;
import com.btr.platform.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final TenantMapper tenantMapper;
    private final SysUserMapper userMapper;
    private final TenantMemberMapper memberMapper;
    private final ContestMapper contestMapper;
    private final RubricMapper rubricMapper;
    private final RubricItemMapper rubricItemMapper;
    private final SponsorMapper sponsorMapper;
    private final CampaignMapper campaignMapper;
    private final CampaignPlacementMapper placementMapper;
    private final ContestSignupMapper signupMapper;
    private final ContestWorkMapper workMapper;
    private final NotificationMapper notificationMapper;
    private final JudgeScoreMapper judgeScoreMapper;
    private final CertificateMapper certificateMapper;
    private final PasswordEncoder passwordEncoder;

    private final String tenantCode;
    private final String tenantName;
    private final String adminAccount;
    private final String adminPassword;
    private final String adminNickname;

    public DataInitializer(TenantMapper tenantMapper,
                           SysUserMapper userMapper,
                           TenantMemberMapper memberMapper,
                           ContestMapper contestMapper,
                           RubricMapper rubricMapper,
                           RubricItemMapper rubricItemMapper,
                           SponsorMapper sponsorMapper,
                           CampaignMapper campaignMapper,
                           CampaignPlacementMapper placementMapper,
                           ContestSignupMapper signupMapper,
                           ContestWorkMapper workMapper,
                           NotificationMapper notificationMapper,
                           JudgeScoreMapper judgeScoreMapper,
                           CertificateMapper certificateMapper,
                           PasswordEncoder passwordEncoder,
                           @Value("${btr.bootstrap.tenant-code:demo}") String tenantCode,
                           @Value("${btr.bootstrap.tenant-name:示例科技}") String tenantName,
                           @Value("${btr.bootstrap.admin-account:admin}") String adminAccount,
                           @Value("${btr.bootstrap.admin-password:admin123}") String adminPassword,
                           @Value("${btr.bootstrap.admin-nickname:平台管理员}") String adminNickname) {
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.memberMapper = memberMapper;
        this.contestMapper = contestMapper;
        this.rubricMapper = rubricMapper;
        this.rubricItemMapper = rubricItemMapper;
        this.sponsorMapper = sponsorMapper;
        this.campaignMapper = campaignMapper;
        this.placementMapper = placementMapper;
        this.signupMapper = signupMapper;
        this.workMapper = workMapper;
        this.notificationMapper = notificationMapper;
        this.judgeScoreMapper = judgeScoreMapper;
        this.certificateMapper = certificateMapper;
        this.passwordEncoder = passwordEncoder;
        this.tenantCode = tenantCode;
        this.tenantName = tenantName;
        this.adminAccount = adminAccount;
        this.adminPassword = adminPassword;
        this.adminNickname = adminNickname;
    }

    @Override
    public void run(String... args) {
        Tenant tenant = tenantMapper.selectOne(new QueryWrapper<Tenant>().eq("code", tenantCode));
        if (tenant == null) {
            tenant = new Tenant();
            tenant.setCode(tenantCode);
            tenant.setName(tenantName);
            tenant.setType("ENTERPRISE");
            tenant.setStatus("ACTIVE");
            tenantMapper.insert(tenant);
            log.info("Bootstrapped tenant: {} ({})", tenant.getName(), tenant.getCode());
        }

        SysUser admin = ensureUser(adminAccount, adminPassword, adminNickname, "PLATFORM_ADMIN");
        SysUser player = ensureUser("player", "player123", "演示选手", null);
        SysUser player2 = ensureUser("player2", "player123", "演示选手二", null);
        SysUser judge = ensureUser("judge", "judge123", "演示评委", null);

        TenantContext.set(tenant.getId());
        try {
            ensureMember(tenant.getId(), admin.getId(), "TENANT_OWNER");
            ensureMember(tenant.getId(), player.getId(), "PLAYER");
            ensureMember(tenant.getId(), player2.getId(), "PLAYER");
            ensureMember(tenant.getId(), judge.getId(), "JUDGE");

            Contest contest = seedContest(tenant.getId());
            seedRubric(contest);
            Sponsor sponsor = seedSponsor();
            seedCampaign(tenant.getId(), sponsor, contest);
            seedSignupAndWork(tenant.getId(), player, judge, contest);
            seedSecondWork(tenant.getId(), player2, contest);
            seedNotifications(tenant.getId(), admin, player, contest);
        } finally {
            TenantContext.clear();
        }
    }

    private SysUser ensureUser(String account, String password, String nickname, String platformRole) {
        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>().eq("account", account));
        if (user == null) {
            user = new SysUser();
            user.setAccount(account);
            user.setPassword(passwordEncoder.encode(password));
            user.setNickname(nickname);
            user.setPlatformRole(platformRole);
            user.setStatus("ACTIVE");
            userMapper.insert(user);
            log.info("Bootstrapped user: {}", account);
        }
        return user;
    }

    private void ensureMember(Long tenantId, Long userId, String role) {
        Long count = memberMapper.selectCount(new QueryWrapper<TenantMember>()
                .eq("tenant_id", tenantId)
                .eq("user_id", userId));
        if (count == null || count == 0) {
            TenantMember member = new TenantMember();
            member.setTenantId(tenantId);
            member.setUserId(userId);
            member.setRole(role);
            member.setStatus("ACTIVE");
            memberMapper.insert(member);
        }
    }

    private Contest seedContest(Long tenantId) {
        Contest contest = contestMapper.selectOne(new QueryWrapper<Contest>()
                .eq("tenant_id", tenantId)
                .eq("title", "2027 企业创新赛"));
        if (contest != null) {
            return contest;
        }
        LocalDateTime now = LocalDateTime.now();
        contest = new Contest();
        contest.tenantId = tenantId;
        contest.title = "2027 企业创新赛";
        contest.summary = "面向企业创新团队的黑客松：用 48 小时做出可演示的原型。";
        contest.cover = null;
        contest.mode = "ONLINE";
        contest.status = "SUBMISSION";
        contest.signupStart = now.minusDays(30);
        contest.signupEnd = now.plusDays(7);
        contest.submitStart = now.minusDays(20);
        contest.submitEnd = now.plusDays(30);
        contest.reviewStart = now.plusDays(31);
        contest.reviewEnd = now.plusDays(38);
        contestMapper.insert(contest);
        log.info("Bootstrapped contest: {}", contest.title);
        return contest;
    }

    private void seedRubric(Contest contest) {
        Long count = rubricMapper.selectCount(new QueryWrapper<Rubric>().eq("contest_id", contest.id));
        if (count != null && count > 0) {
            return;
        }
        Rubric rubric = new Rubric();
        rubric.tenantId = contest.tenantId;
        rubric.contestId = contest.id;
        rubric.name = "创新赛评分表";
        rubric.mode = "WEIGHTED";
        rubric.status = "ACTIVE";
        rubricMapper.insert(rubric);

        item(rubric, "innovation", "创新性", "0.30", 0);
        item(rubric, "completion", "完成度", "0.40", 1);
        item(rubric, "presentation", "展示效果", "0.30", 2);
        log.info("Bootstrapped rubric for contest {}", contest.id);
    }

    private void item(Rubric rubric, String key, String label, String weight, int sort) {
        RubricItem item = new RubricItem();
        item.tenantId = rubric.tenantId;
        item.rubricId = rubric.id;
        item.itemKey = key;
        item.label = label;
        item.weight = new BigDecimal(weight);
        item.minScore = BigDecimal.ZERO;
        item.maxScore = new BigDecimal("100");
        item.sortOrder = sort;
        rubricItemMapper.insert(item);
    }

    private Sponsor seedSponsor() {
        Sponsor sponsor = sponsorMapper.selectOne(new QueryWrapper<Sponsor>().eq("name", "星云科技"));
        if (sponsor != null) {
            return sponsor;
        }
        sponsor = new Sponsor();
        sponsor.name = "星云科技";
        sponsor.industry = "云计算";
        sponsor.contactName = "林经理";
        sponsor.contactEmail = "sponsor@example.com";
        sponsor.status = "ACTIVE";
        sponsorMapper.insert(sponsor);
        log.info("Bootstrapped sponsor: {}", sponsor.name);
        return sponsor;
    }

    private void seedCampaign(Long tenantId, Sponsor sponsor, Contest contest) {
        Long count = campaignMapper.selectCount(new QueryWrapper<Campaign>()
                .eq("sponsor_id", sponsor.id)
                .eq("name", "2027 企业创新赛冠名"));
        if (count != null && count > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Campaign campaign = new Campaign();
        campaign.sponsorId = sponsor.id;
        campaign.tenantId = tenantId;
        campaign.name = "2027 企业创新赛冠名";
        campaign.type = "TITLE";
        campaign.budget = new BigDecimal("200000");
        campaign.status = "APPROVED";
        campaign.creativeText = "星云科技 · 让创意落地";
        campaign.creativeUrl = "https://example.com";
        campaign.startAt = now.minusDays(30);
        campaign.endAt = now.plusDays(60);
        campaignMapper.insert(campaign);

        CampaignPlacement placement = new CampaignPlacement();
        placement.campaignId = campaign.id;
        placement.slot = "contest.title";
        placement.contestId = contest.id;
        placement.weight = 100;
        placement.startAt = campaign.startAt;
        placement.endAt = campaign.endAt;
        placementMapper.insert(placement);

        CampaignPlacement home = new CampaignPlacement();
        home.campaignId = campaign.id;
        home.slot = "home.banner";
        home.contestId = null;
        home.weight = 80;
        home.startAt = campaign.startAt;
        home.endAt = campaign.endAt;
        placementMapper.insert(home);
        log.info("Bootstrapped campaign: {}", campaign.name);
    }

    private void seedSignupAndWork(Long tenantId, SysUser player, SysUser judge, Contest contest) {
        ContestSignup signup = signupMapper.selectOne(new QueryWrapper<ContestSignup>()
                .eq("contest_id", contest.id)
                .eq("user_id", player.getId()));
        if (signup == null) {
            signup = new ContestSignup();
            signup.tenantId = tenantId;
            signup.contestId = contest.id;
            signup.userId = player.getId();
            signup.teamName = "星河小队";
            signup.contact = "player@example.com";
            signup.status = "APPROVED";
            signupMapper.insert(signup);
        }

        ContestWork work = workMapper.selectOne(new QueryWrapper<ContestWork>()
                .eq("signup_id", signup.id));
        if (work == null) {
            work = new ContestWork();
            work.tenantId = tenantId;
            work.contestId = contest.id;
            work.signupId = signup.id;
            work.userId = player.getId();
            work.title = "智能巡检机器人";
            work.description = "面向园区巡检的自主移动机器人，支持视觉识别与异常上报。";
            work.videoUrl = "https://example.com/demo.mp4";
            work.fileUrl = null;
            work.status = "SUBMITTED";
            workMapper.insert(work);
            log.info("Bootstrapped work for contest {}", contest.id);
        }

        Long scoreCount = judgeScoreMapper.selectCount(new QueryWrapper<JudgeScore>()
                .eq("work_id", work.id)
                .eq("judge_id", judge.getId()));
        if (scoreCount == null || scoreCount == 0) {
            JudgeScore score = new JudgeScore();
            score.tenantId = tenantId;
            score.contestId = contest.id;
            score.workId = work.id;
            score.judgeId = judge.getId();
            score.totalScore = new BigDecimal("91.5");
            score.comment = "结构清晰，演示完整";
            judgeScoreMapper.insert(score);

            work.totalScore = new BigDecimal("91.5");
            workMapper.updateById(work);
            log.info("Bootstrapped judge score for work {}", work.id);
        }

        Long certCount = certificateMapper.selectCount(new QueryWrapper<Certificate>()
                .eq("contest_id", contest.id)
                .eq("user_id", player.getId()));
        if (certCount == null || certCount == 0) {
            Certificate certificate = new Certificate();
            certificate.tenantId = tenantId;
            certificate.contestId = contest.id;
            certificate.userId = player.getId();
            certificate.award = "一等奖";
            certificate.code = "BTR-" + tenantId + "-DEMO";
            certificate.status = "ISSUED";
            certificate.issuedAt = LocalDateTime.now();
            certificateMapper.insert(certificate);
            log.info("Bootstrapped certificate: {}", certificate.code);
        }
    }

    private void seedSecondWork(Long tenantId, SysUser player, Contest contest) {
        ContestSignup signup = signupMapper.selectOne(new QueryWrapper<ContestSignup>()
                .eq("contest_id", contest.id)
                .eq("user_id", player.getId()));
        if (signup == null) {
            signup = new ContestSignup();
            signup.tenantId = tenantId;
            signup.contestId = contest.id;
            signup.userId = player.getId();
            signup.teamName = "绿洲小队";
            signup.contact = "player2@example.com";
            signup.status = "APPROVED";
            signupMapper.insert(signup);
        }
        ContestWork work = workMapper.selectOne(new QueryWrapper<ContestWork>()
                .eq("signup_id", signup.id));
        if (work == null) {
            work = new ContestWork();
            work.tenantId = tenantId;
            work.contestId = contest.id;
            work.signupId = signup.id;
            work.userId = player.getId();
            work.title = "园区能耗优化方案";
            work.description = "基于用电数据的园区能耗分析与节能建议，目标降低 12% 峰值负荷。";
            work.videoUrl = "https://example.com/demo2.mp4";
            work.status = "SUBMITTED";
            workMapper.insert(work);
            log.info("Bootstrapped second work for contest {}", contest.id);
        }
    }

    private void seedNotifications(Long tenantId, SysUser admin, SysUser player, Contest contest) {
        Long count = notificationMapper.selectCount(new QueryWrapper<Notification>()
                .eq("user_id", admin.getId()));
        if (count != null && count > 0) {
            return;
        }
        Notification notification = new Notification();
        notification.tenantId = tenantId;
        notification.userId = admin.getId();
        notification.type = "SYSTEM";
        notification.title = "欢迎使用 BTR";
        notification.body = "演示赛事《" + contest.title + "》已创建，可前往赛事管理查看。";
        notification.link = "/admin/contests";
        notificationMapper.insert(notification);

        Notification playerNotification = new Notification();
        playerNotification.tenantId = tenantId;
        playerNotification.userId = player.getId();
        playerNotification.type = "AWARD";
        playerNotification.title = "作品获奖通知";
        playerNotification.body = "你的作品《智能巡检机器人》获得一等奖，证书已签发。";
        playerNotification.link = "/me";
        notificationMapper.insert(playerNotification);
    }
}
