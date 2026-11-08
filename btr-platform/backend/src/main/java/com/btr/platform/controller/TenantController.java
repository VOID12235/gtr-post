package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.TenantDtos;
import com.btr.platform.service.TenantService;
import com.btr.platform.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/api/v1/platform/tenants")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<List<TenantDtos.TenantView>> list() {
        return ApiResponse.ok(tenantService.list());
    }

    @PostMapping("/api/v1/platform/tenants")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<TenantDtos.TenantView> create(@Valid @RequestBody TenantDtos.CreateTenantRequest request) {
        return ApiResponse.ok(tenantService.create(request));
    }

    @GetMapping("/api/v1/tenant/profile")
    public ApiResponse<TenantDtos.TenantView> profile() {
        return ApiResponse.ok(tenantService.profile(TenantContext.require()));
    }
}
