package com.btr.platform.security;

public record UserPrincipal(
        Long userId,
        Long tenantId,
        String account,
        String nickname,
        String role,
        String platformRole
) {
}
