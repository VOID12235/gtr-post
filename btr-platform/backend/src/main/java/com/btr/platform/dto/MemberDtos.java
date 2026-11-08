package com.btr.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class MemberDtos {

    private MemberDtos() {
    }

    public record CreateMemberRequest(
            @NotBlank String account,
            @NotBlank String password,
            @NotBlank String nickname,
            @Email String email,
            @NotBlank String role
    ) {
    }

    public record MemberView(
            Long id,
            Long userId,
            String account,
            String nickname,
            String email,
            String role,
            String status
    ) {
    }

    public record RoleRequest(@NotBlank String role) {
    }

    public record StatusRequest(@NotBlank String status) {
    }
}
