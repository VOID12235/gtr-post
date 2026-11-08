package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.TenantDtos;
import com.btr.platform.entity.SysUser;
import com.btr.platform.entity.Tenant;
import com.btr.platform.entity.TenantMember;
import com.btr.platform.mapper.SysUserMapper;
import com.btr.platform.mapper.TenantMapper;
import com.btr.platform.mapper.TenantMemberMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
public class TenantService {

    private final TenantMapper tenantMapper;
    private final SysUserMapper userMapper;
    private final TenantMemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public TenantService(TenantMapper tenantMapper,
                         SysUserMapper userMapper,
                         TenantMemberMapper memberMapper,
                         PasswordEncoder passwordEncoder) {
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<TenantDtos.TenantView> list() {
        return tenantMapper.selectList(new LambdaQueryWrapper<Tenant>()
                        .orderByDesc(Tenant::getId))
                .stream()
                .map(AuthService::toView)
                .toList();
    }

    public TenantDtos.TenantView profile(long tenantId) {
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BizException(ErrorCode.TENANT_NOT_FOUND);
        }
        return AuthService.toView(tenant);
    }

    public Tenant requireByCode(String code) {
        Tenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getCode, code));
        if (tenant == null || !"ACTIVE".equals(tenant.getStatus())) {
            throw new BizException(ErrorCode.TENANT_NOT_FOUND);
        }
        return tenant;
    }

    public <T> T withTenant(String tenantCode, Supplier<T> action) {
        Tenant tenant = requireByCode(tenantCode);
        TenantContext.set(tenant.getId());
        try {
            return action.get();
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public TenantDtos.TenantView create(TenantDtos.CreateTenantRequest request) {
        Long exists = tenantMapper.selectCount(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getCode, request.code()));
        if (exists != null && exists > 0) {
            throw new BizException(ErrorCode.CONFLICT, "租户编码已存在");
        }

        Tenant tenant = new Tenant();
        tenant.setCode(request.code());
        tenant.setName(request.name());
        tenant.setType(request.type());
        tenant.setStatus("ACTIVE");
        tenant.setContactName(request.contactName());
        tenant.setContactEmail(request.contactEmail());
        tenantMapper.insert(tenant);

        if (request.ownerAccount() != null && !request.ownerAccount().isBlank()) {
            if (request.ownerPassword() == null || request.ownerPassword().length() < 6) {
                throw new BizException(ErrorCode.BAD_REQUEST, "租户所有者密码至少 6 位");
            }
            Long userExists = userMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getAccount, request.ownerAccount()));
            if (userExists != null && userExists > 0) {
                throw new BizException(ErrorCode.CONFLICT, "所有者账号已存在");
            }
            SysUser owner = new SysUser();
            owner.setAccount(request.ownerAccount());
            owner.setPassword(passwordEncoder.encode(request.ownerPassword()));
            owner.setNickname(request.ownerNickname() == null || request.ownerNickname().isBlank()
                    ? request.ownerAccount() : request.ownerNickname());
            owner.setEmail(request.contactEmail());
            owner.setStatus("ACTIVE");
            userMapper.insert(owner);

            Long previous = TenantContext.get();
            TenantContext.set(tenant.getId());
            try {
                TenantMember member = new TenantMember();
                member.setTenantId(tenant.getId());
                member.setUserId(owner.getId());
                member.setRole("TENANT_OWNER");
                member.setStatus("ACTIVE");
                memberMapper.insert(member);
            } finally {
                if (previous == null) {
                    TenantContext.clear();
                } else {
                    TenantContext.set(previous);
                }
            }
        }
        return AuthService.toView(tenant);
    }
}
