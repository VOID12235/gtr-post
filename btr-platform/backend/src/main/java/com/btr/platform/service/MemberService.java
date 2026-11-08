package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.MemberDtos;
import com.btr.platform.entity.SysUser;
import com.btr.platform.entity.TenantMember;
import com.btr.platform.mapper.SysUserMapper;
import com.btr.platform.mapper.TenantMemberMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class MemberService {

    private static final Set<String> ROLES = Set.of("TENANT_OWNER", "TENANT_ADMIN", "ORGANIZER", "JUDGE", "PLAYER");

    private final TenantMemberMapper memberMapper;
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberService(TenantMemberMapper memberMapper,
                         SysUserMapper userMapper,
                         PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<MemberDtos.MemberView> list() {
        return memberMapper.selectList(new QueryWrapper<TenantMember>()
                        .orderByDesc("id"))
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public MemberDtos.MemberView create(MemberDtos.CreateMemberRequest request) {
        if (!ROLES.contains(request.role())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "未知角色");
        }
        if (request.password().length() < 6) {
            throw new BizException(ErrorCode.BAD_REQUEST, "密码至少 6 位");
        }
        SysUser user = userMapper.selectOne(new QueryWrapper<SysUser>()
                .eq("account", request.account()));
        if (user == null) {
            user = new SysUser();
            user.setAccount(request.account());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setNickname(request.nickname());
            user.setEmail(request.email());
            user.setStatus("ACTIVE");
            userMapper.insert(user);
        }

        TenantMember existing = memberMapper.selectOne(new QueryWrapper<TenantMember>()
                .eq("tenant_id", TenantContext.require())
                .eq("user_id", user.getId()));
        if (existing != null) {
            existing.setRole(request.role());
            existing.setStatus("ACTIVE");
            memberMapper.updateById(existing);
            return toView(existing);
        }

        TenantMember member = new TenantMember();
        member.setTenantId(TenantContext.require());
        member.setUserId(user.getId());
        member.setRole(request.role());
        member.setStatus("ACTIVE");
        memberMapper.insert(member);
        return toView(member);
    }

    @Transactional
    public MemberDtos.MemberView updateRole(Long id, String role) {
        if (!ROLES.contains(role)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "未知角色");
        }
        TenantMember member = memberMapper.selectById(id);
        if (member == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "成员不存在");
        }
        member.setRole(role);
        memberMapper.updateById(member);
        return toView(member);
    }

    @Transactional
    public MemberDtos.MemberView updateStatus(Long id, String status) {
        TenantMember member = memberMapper.selectById(id);
        if (member == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "成员不存在");
        }
        member.setStatus(status);
        memberMapper.updateById(member);
        return toView(member);
    }

    private MemberDtos.MemberView toView(TenantMember member) {
        SysUser user = userMapper.selectById(member.getUserId());
        return new MemberDtos.MemberView(
                member.getId(),
                member.getUserId(),
                user == null ? null : user.getAccount(),
                user == null ? null : user.getNickname(),
                user == null ? null : user.getEmail(),
                member.getRole(),
                member.getStatus()
        );
    }
}
