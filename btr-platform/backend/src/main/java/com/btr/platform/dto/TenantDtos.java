package com.btr.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class TenantDtos {

    private TenantDtos() {
    }

    public record CreateTenantRequest(
            @NotBlank @Size(max = 64) String code,
            @NotBlank @Size(max = 128) String name,
            @NotBlank String type,
            String contactName,
            @Email String contactEmail,
            String ownerAccount,
            String ownerPassword,
            String ownerNickname
    ) {
    }

    public record TenantView(
            Long id,
            String code,
            String name,
            String type,
            String status,
            String contactName,
            String contactEmail,
            LocalDateTime createdAt
    ) {
    }
}
