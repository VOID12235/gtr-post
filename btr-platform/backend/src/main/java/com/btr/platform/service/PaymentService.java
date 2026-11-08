package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.entity.Contest;
import com.btr.platform.entity.ContestSignup;
import com.btr.platform.entity.PaymentOrder;
import com.btr.platform.entity.SysUser;
import com.btr.platform.mapper.ContestSignupMapper;
import com.btr.platform.mapper.PaymentOrderMapper;
import com.btr.platform.mapper.SysUserMapper;
import com.btr.platform.security.UserPrincipal;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentOrderMapper orderMapper;
    private final ContestSignupMapper signupMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public PaymentService(PaymentOrderMapper orderMapper,
                          ContestSignupMapper signupMapper,
                          SysUserMapper userMapper,
                          NotificationService notificationService,
                          EmailService emailService) {
        this.orderMapper = orderMapper;
        this.signupMapper = signupMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Transactional
    public PaymentOrder createForContest(Contest contest, UserPrincipal principal) {
        if (!Boolean.TRUE.equals(contest.feeEnabled)
                || contest.feeAmount == null
                || contest.feeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        PaymentOrder order = new PaymentOrder();
        order.tenantId = TenantContext.require();
        order.contestId = contest.id;
        order.userId = principal.userId();
        order.amount = contest.feeAmount;
        order.status = "PENDING";
        order.method = "MANUAL";
        order.createdAt = LocalDateTime.now();
        orderMapper.insert(order);
        return order;
    }

    public List<PaymentOrder> mine(Long userId) {
        return orderMapper.selectList(new QueryWrapper<PaymentOrder>()
                .eq("user_id", userId)
                .orderByDesc("id"));
    }

    public List<PaymentOrder> listByContest(Long contestId) {
        return orderMapper.selectList(new QueryWrapper<PaymentOrder>()
                .eq("contest_id", contestId)
                .orderByDesc("id"));
    }

    @Transactional
    public PaymentOrder markPaid(Long id, String reference) {
        PaymentOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "支付单不存在");
        }
        if ("PAID".equals(order.status)) {
            return order;
        }
        order.status = "PAID";
        order.reference = reference;
        order.paidAt = LocalDateTime.now();
        orderMapper.updateById(order);

        ContestSignup signup = signupMapper.selectOne(new QueryWrapper<ContestSignup>()
                .eq("contest_id", order.contestId)
                .eq("user_id", order.userId));
        if (signup != null) {
            signup.status = "APPROVED";
            signupMapper.updateById(signup);
        }

        notificationService.create(order.userId, "PAYMENT",
                "报名支付已确认",
                "你的报名已生效，可以提交作品了。",
                "/me");
        SysUser user = userMapper.selectById(order.userId);
        if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
            emailService.send(user.getEmail(), "BTR 报名支付已确认",
                    "<p>你的报名支付已确认，报名已生效。</p>");
        }
        return order;
    }

    @Transactional
    public PaymentOrder refund(Long id) {
        PaymentOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "支付单不存在");
        }
        order.status = "REFUNDED";
        orderMapper.updateById(order);
        ContestSignup signup = signupMapper.selectOne(new QueryWrapper<ContestSignup>()
                .eq("contest_id", order.contestId)
                .eq("user_id", order.userId));
        if (signup != null) {
            signup.status = "CANCELED";
            signupMapper.updateById(signup);
        }
        return order;
    }
}
