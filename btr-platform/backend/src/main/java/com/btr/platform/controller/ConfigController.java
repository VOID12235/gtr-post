package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.service.ConfigService;
import com.btr.platform.service.EmailService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/config")
@PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
public class ConfigController {

    private final ConfigService configService;
    private final EmailService emailService;

    public ConfigController(ConfigService configService, EmailService emailService) {
        this.configService = configService;
        this.emailService = emailService;
    }

    @GetMapping("/{group}")
    public ApiResponse<Map<String, String>> get(@PathVariable String group) {
        return ApiResponse.ok(configService.getGroup(group));
    }

    @PutMapping("/{group}")
    public ApiResponse<Map<String, String>> update(@PathVariable String group,
                                                    @RequestBody Map<String, String> values) {
        return ApiResponse.ok(configService.updateGroup(group, values));
    }

    @PostMapping("/mail/test")
    public ApiResponse<Map<String, Object>> testMail(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        boolean sent = to != null && !to.isBlank() && emailService.sendTest(to);
        return ApiResponse.ok(Map.of("success", sent));
    }
}
