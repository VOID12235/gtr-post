package com.btr.platform.tenant;

import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long get() {
        return CURRENT_TENANT.get();
    }

    public static long require() {
        Long tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new BizException(ErrorCode.TENANT_NOT_FOUND);
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
