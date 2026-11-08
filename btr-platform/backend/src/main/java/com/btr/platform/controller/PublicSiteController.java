package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.service.ConfigService;
import com.btr.platform.service.TenantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PublicSiteController {

    private final ConfigService configService;
    private final TenantService tenantService;

    public PublicSiteController(ConfigService configService, TenantService tenantService) {
        this.configService = configService;
        this.tenantService = tenantService;
    }

    @GetMapping("/api/v1/public/site")
    public ApiResponse<Map<String, String>> site(@RequestParam(defaultValue = "demo") String tenantCode) {
        return ApiResponse.ok(tenantService.withTenant(tenantCode, configService::getPublicSite));
    }
}
