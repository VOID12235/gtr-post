package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.ContestSignup;
import com.btr.platform.security.SecurityUtils;
import com.btr.platform.service.SignupService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/api/v1/signups")
    public ApiResponse<ContestSignup> signup(@Valid @RequestBody P1Dtos.SignupRequest request) {
        return ApiResponse.ok(signupService.signup(request, SecurityUtils.current()));
    }

    @GetMapping("/api/v1/signups/mine")
    public ApiResponse<List<ContestSignup>> mine() {
        return ApiResponse.ok(signupService.mine(SecurityUtils.current().userId()));
    }

    @GetMapping("/api/v1/admin/signups")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<ContestSignup>> list(@RequestParam Long contestId) {
        return ApiResponse.ok(signupService.listByContest(contestId));
    }
}
