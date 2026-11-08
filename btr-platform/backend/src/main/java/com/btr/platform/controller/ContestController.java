package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Contest;
import com.btr.platform.service.ContestService;
import com.btr.platform.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ContestController {

    private final ContestService contestService;
    private final TenantService tenantService;

    public ContestController(ContestService contestService, TenantService tenantService) {
        this.contestService = contestService;
        this.tenantService = tenantService;
    }

    @GetMapping("/api/v1/admin/contests")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<Contest>> list() {
        return ApiResponse.ok(contestService.list());
    }

    @PostMapping("/api/v1/admin/contests")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<Contest> create(@Valid @RequestBody P1Dtos.CreateContestRequest request) {
        return ApiResponse.ok(contestService.create(request));
    }

    @GetMapping("/api/v1/admin/contests/{id}")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<Contest> get(@PathVariable Long id) {
        return ApiResponse.ok(contestService.get(id));
    }

    @PutMapping("/api/v1/admin/contests/{id}/status")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<Contest> updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody P1Dtos.StatusRequest request) {
        return ApiResponse.ok(contestService.updateStatus(id, request.status()));
    }

    @GetMapping("/api/v1/public/contests")
    public ApiResponse<List<P1Dtos.ContestView>> publicList(@RequestParam(defaultValue = "demo") String tenantCode) {
        return ApiResponse.ok(tenantService.withTenant(tenantCode, () -> contestService.publicList()
                .stream()
                .map(contestService::toView)
                .toList()));
    }

    @GetMapping("/api/v1/public/contests/{id}")
    public ApiResponse<P1Dtos.ContestView> publicGet(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "demo") String tenantCode) {
        return ApiResponse.ok(tenantService.withTenant(tenantCode,
                () -> contestService.toView(contestService.get(id))));
    }
}
