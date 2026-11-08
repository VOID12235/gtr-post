package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.service.CertificateService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping("/api/v1/admin/certificates")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<P1Dtos.CertificateView>> list(@RequestParam Long contestId) {
        return ApiResponse.ok(certificateService.listByContest(contestId));
    }

    @PostMapping("/api/v1/admin/certificates/generate")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<P1Dtos.CertificateView>> generate(@Valid @RequestBody P1Dtos.CertificateRequest request) {
        return ApiResponse.ok(certificateService.generate(request));
    }

    @GetMapping("/api/v1/public/certificates/{code}")
    public ApiResponse<P1Dtos.CertificateView> verify(@PathVariable String code) {
        return ApiResponse.ok(certificateService.verify(code));
    }
}
