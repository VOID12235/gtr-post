package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.ContestWork;
import com.btr.platform.security.SecurityUtils;
import com.btr.platform.service.TenantService;
import com.btr.platform.service.WorkService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WorkController {

    private final WorkService workService;
    private final TenantService tenantService;

    public WorkController(WorkService workService, TenantService tenantService) {
        this.workService = workService;
        this.tenantService = tenantService;
    }

    @PostMapping("/api/v1/works")
    public ApiResponse<ContestWork> upsert(@Valid @RequestBody P1Dtos.WorkRequest request) {
        return ApiResponse.ok(workService.upsert(request, SecurityUtils.current()));
    }

    @GetMapping("/api/v1/works/mine")
    public ApiResponse<List<ContestWork>> mine() {
        return ApiResponse.ok(workService.mine(SecurityUtils.current().userId()));
    }

    @GetMapping("/api/v1/admin/works")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<ContestWork>> list(@RequestParam Long contestId) {
        return ApiResponse.ok(workService.listByContest(contestId));
    }

    @GetMapping("/api/v1/public/works")
    public ApiResponse<List<P1Dtos.WorkView>> publicList(@RequestParam Long contestId,
                                                         @RequestParam(defaultValue = "demo") String tenantCode) {
        return ApiResponse.ok(tenantService.withTenant(tenantCode,
                () -> workService.publicList(contestId)));
    }
}
