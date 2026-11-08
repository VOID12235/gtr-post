package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.entity.Notification;
import com.btr.platform.mapper.NotificationMapper;
import com.btr.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public void create(Long userId, String type, String title, String body, String link) {
        Notification notification = new Notification();
        notification.tenantId = TenantContext.require();
        notification.userId = userId;
        notification.type = type;
        notification.title = title;
        notification.body = body;
        notification.link = link;
        notificationMapper.insert(notification);
    }

    public List<Notification> list(Long userId) {
        return notificationMapper.selectList(new QueryWrapper<Notification>()
                .eq("user_id", userId)
                .orderByDesc("id"));
    }

    public long unread(Long userId) {
        Long count = notificationMapper.selectCount(new QueryWrapper<Notification>()
                .eq("user_id", userId)
                .isNull("read_at"));
        return count == null ? 0 : count;
    }

    public void markRead(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null || !userId.equals(notification.userId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "通知不存在");
        }
        notification.readAt = LocalDateTime.now();
        notificationMapper.updateById(notification);
    }
}
