package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.AuthDtos;
import com.btr.platform.dto.TenantDtos;
import com.btr.platform.entity.SysUser;
import com.btr.platform.entity.Tenant;
import com.btr.platform.entity.TenantMember;
import com.btr.platform.mapper.SysUserMapper;
import com.btr.platform.mapper.TenantMapper;
import com.btr.platform.mapper.TenantMemberMapper;
import com.btr.platform.security.JwtService;
import com.btr.platform.security.UserPrincipal;
import com.btr.platform.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final TenantMapper tenantMapper;
    private final SysUserMapper userMapper;
    private final TenantMemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SliderCaptchaService captchaService;
    private final VerificationCodeService codeService;
    private final EmailService emailService;
    private final boolean captchaEnabled;

    public AuthService(TenantMapper tenantMapper,
                       SysUserMapper userMapper,
                       TenantMemberMapper memberMapper,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       SliderCaptchaService captchaService,
                       VerificationCodeService codeService,
                       EmailService emailService,
                       @Value("${btr.captcha.enabled:true}") boolean captchaEnabled) {
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.captchaService = captchaService;
        this.codeService = codeService;
        this.emailService = emailService;
        this.captchaEnabled = captchaEnabled;
    }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        checkCaptcha(request.sliderTicket());

        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, request.account()));
        if (user == null
                || !"ACTIVE".equals(user.getStatus())
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }

        Tenant tenant;
        if (request.tenantCode() != null && !request.tenantCode().isBlank()) {
            tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>()
                    .eq(Tenant::getCode, request.tenantCode()));
        } else {
            TenantMember first = memberMapper.selectFirstActiveByUser(user.getId());
            tenant = first == null ? null : tenantMapper.selectById(first.getTenantId());
        }
        if (tenant == null || !"ACTIVE".equals(tenant.getStatus())) {
            throw new BizException(ErrorCode.TENANT_NOT_FOUND);
        }

        TenantContext.set(tenant.getId());
        try {
            TenantMember member = memberMapper.selectOne(new LambdaQueryWrapper<TenantMember>()
                    .eq(TenantMember::getUserId, user.getId())
                    .eq(TenantMember::getStatus, "ACTIVE"));
            if (member == null) {
                throw new BizException(ErrorCode.FORBIDDEN, "该账号不属于此租户");
            }

            UserPrincipal principal = new UserPrincipal(
                    user.getId(),
                    tenant.getId(),
                    user.getAccount(),
                    user.getNickname(),
                    member.getRole(),
                    user.getPlatformRole()
            );
            String token = jwtService.generate(principal);
            return new AuthDtos.LoginResponse(
                    token,
                    new AuthDtos.UserView(user.getId(), user.getAccount(), user.getNickname(),
                            member.getRole(), user.getPlatformRole()),
                    toView(tenant)
            );
        } finally {
            TenantContext.clear();
        }
    }

    public void sendRegisterCode(String tenantCode, String email, String sliderTicket) {
        checkCaptcha(sliderTicket);
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isBlank() || !normalized.contains("@")) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请输入正确的邮箱");
        }
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, normalized));
        if (exists != null && exists > 0) {
            throw new BizException(ErrorCode.CONFLICT, "该邮箱已注册");
        }
        withTenantMailContext(tenantCode, () -> {
            String code = codeService.issue("register", normalized);
            boolean sent = emailService.send(normalized, "BTR 注册验证码",
                    "<p>你的注册验证码是：<b style=\"font-size:20px\">" + code + "</b></p>"
                            + "<p>10 分钟内有效，请勿转告他人。</p>");
            if (!sent) {
                codeService.remove("register", normalized);
                throw new BizException(ErrorCode.INTERNAL,
                        "邮件服务未配置或发送失败，请联系管理员在后台配置 SMTP");
            }
        });
    }

    public void forgotPassword(String tenantCode, String email, String sliderTicket) {
        checkCaptcha(sliderTicket);
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isBlank() || !normalized.contains("@")) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请输入正确的邮箱");
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, normalized));
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            // 防枚举：不透露邮箱是否存在
            return;
        }
        withTenantMailContext(tenantCode, () -> {
            String code = codeService.issue("reset", normalized);
            boolean sent = emailService.send(normalized, "BTR 重置密码验证码",
                    "<p>你正在重置 BTR 密码，验证码：<b style=\"font-size:20px\">" + code + "</b></p>"
                            + "<p>10 分钟内有效。如非本人操作，请忽略。</p>");
            if (!sent) {
                codeService.remove("reset", normalized);
                throw new BizException(ErrorCode.INTERNAL,
                        "邮件服务未配置或发送失败，请联系管理员在后台配置 SMTP");
            }
        });
    }

    private void withTenantMailContext(String tenantCode, Runnable action) {
        String code = tenantCode == null || tenantCode.isBlank() ? "demo" : tenantCode;
        Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getCode, code));
        if (tenant == null || !"ACTIVE".equals(tenant.getStatus())) {
            throw new BizException(ErrorCode.TENANT_NOT_FOUND);
        }
        Long previous = TenantContext.get();
        TenantContext.set(tenant.getId());
        try {
            action.run();
        } finally {
            if (previous == null) {
                TenantContext.clear();
            } else {
                TenantContext.set(previous);
            }
        }
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (newPassword == null || newPassword.length() < 6) {
            throw new BizException(ErrorCode.BAD_REQUEST, "新密码至少 6 位");
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, normalized));
        if (user == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "验证码错误");
        }
        codeService.verify("reset", normalized, code);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public AuthDtos.UserView me(UserPrincipal principal) {
        return new AuthDtos.UserView(
                principal.userId(),
                principal.account(),
                principal.nickname(),
                principal.role(),
                principal.platformRole()
        );
    }

    @Transactional
    public void changePassword(UserPrincipal principal, AuthDtos.ChangePasswordRequest request) {
        SysUser user = userMapper.selectById(principal.userId());
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "当前密码不正确");
        }
        if (request.newPassword() == null || request.newPassword().length() < 6) {
            throw new BizException(ErrorCode.BAD_REQUEST, "新密码至少 6 位");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
    }

    @Transactional
    public AuthDtos.LoginResponse register(AuthDtos.RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, email));
        if (exists != null && exists > 0) {
            throw new BizException(ErrorCode.CONFLICT, "该邮箱已注册");
        }
        codeService.verify("register", email, request.code());

        String tenantCode = request.tenantCode() == null || request.tenantCode().isBlank()
                ? "demo" : request.tenantCode();
        Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getCode, tenantCode));
        if (tenant == null || !"ACTIVE".equals(tenant.getStatus())) {
            throw new BizException(ErrorCode.TENANT_NOT_FOUND);
        }

        SysUser user = new SysUser();
        user.setAccount(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setEmail(email);
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        TenantContext.set(tenant.getId());
        try {
            TenantMember member = new TenantMember();
            member.setTenantId(tenant.getId());
            member.setUserId(user.getId());
            member.setRole("PLAYER");
            member.setStatus("ACTIVE");
            memberMapper.insert(member);

            UserPrincipal principal = new UserPrincipal(
                    user.getId(), tenant.getId(), user.getAccount(), user.getNickname(),
                    "PLAYER", user.getPlatformRole());
            String token = jwtService.generate(principal);
            return new AuthDtos.LoginResponse(
                    token,
                    new AuthDtos.UserView(user.getId(), user.getAccount(), user.getNickname(),
                            "PLAYER", user.getPlatformRole()),
                    toView(tenant)
            );
        } finally {
            TenantContext.clear();
        }
    }

    private void checkCaptcha(String ticket) {
        if (!captchaEnabled) {
            return;
        }
        if (!captchaService.consume(ticket)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请先完成滑动验证");
        }
    }

    public static TenantDtos.TenantView toView(Tenant tenant) {
        return new TenantDtos.TenantView(
                tenant.getId(),
                tenant.getCode(),
                tenant.getName(),
                tenant.getType(),
                tenant.getStatus(),
                tenant.getContactName(),
                tenant.getContactEmail(),
                tenant.getCreatedAt()
        );
    }
}
