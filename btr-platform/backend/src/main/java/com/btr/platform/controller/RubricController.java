package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.service.RubricService;
import com.btr.platform.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RubricController {

    private final RubricService rubricService;
    private final TenantService tenantService;

    public RubricController(RubricService rubricService, TenantService tenantService) {
        this.rubricService = rubricService;
        this.tenantService = tenantService;
    }

    @GetMapping("/api/v1/admin/rubrics")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<P1Dtos.RubricView>> list(@RequestParam Long contestId) {
        return ApiResponse.ok(rubricService.listByContest(contestId));
    }

    @PostMapping("/api/v1/admin/rubrics")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<P1Dtos.RubricView> create(@Valid @RequestBody P1Dtos.CreateRubricRequest request) {
        return ApiResponse.ok(rubricService.create(request));
    }

    @GetMapping("/api/v1/public/rubrics")
    public ApiResponse<List<P1Dtos.RubricView>> publicList(@RequestParam Long contestId,
                                                           @RequestParam(defaultValue = "demo") String tenantCode) {
        return ApiResponse.ok(tenantService.withTenant(tenantCode,
                () -> rubricService.listByContest(contestId)));
    }
}
