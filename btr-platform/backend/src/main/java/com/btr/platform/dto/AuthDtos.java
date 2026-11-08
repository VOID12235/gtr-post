package com.btr.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            String tenantCode,
            @NotBlank String account,
            @NotBlank String password,
            String sliderTicket
    ) {
    }

    public record RegisterRequest(
            String tenantCode,
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String nickname,
            @NotBlank String code,
            String sliderTicket
    ) {
    }

    public record SendCodeRequest(
            String tenantCode,
            @NotBlank @Email String email,
            String sliderTicket
    ) {
    }

    public record ForgotPasswordRequest(
            String tenantCode,
            @NotBlank @Email String email,
            String sliderTicket
    ) {
    }

    public record ResetPasswordRequest(
            @NotBlank @Email String email,
            @NotBlank String code,
            @NotBlank String newPassword
    ) {
    }

    public record SliderVerifyRequest(
            @NotBlank String challengeId,
            @NotNull Double position,
            @NotNull Long duration
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank String newPassword
    ) {
    }

    public record LoginResponse(
            String accessToken,
            UserView user,
            TenantDtos.TenantView tenant
    ) {
    }

    public record UserView(
            Long id,
            String account,
            String nickname,
            String role,
            String platformRole
    ) {
    }
}
