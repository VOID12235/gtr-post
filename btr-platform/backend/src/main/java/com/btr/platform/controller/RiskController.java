package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.InsurancePolicy;
import com.btr.platform.entity.PrizePool;
import com.btr.platform.entity.SafetyPlan;
import com.btr.platform.service.RiskService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/api/v1/admin/risk/{contestId}")
    public ApiResponse<Map<String, Object>> get(@PathVariable Long contestId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("prize", riskService.prize(contestId));
        body.put("insurances", riskService.insurances(contestId));
        body.put("safety", riskService.safety(contestId));
        return ApiResponse.ok(body);
    }

    @PutMapping("/api/v1/admin/risk/{contestId}/prize")
    public ApiResponse<PrizePool> savePrize(@PathVariable Long contestId,
                                            @Valid @RequestBody P1Dtos.PrizeRequest request) {
        return ApiResponse.ok(riskService.savePrize(contestId, request));
    }

    @PutMapping("/api/v1/admin/risk/{contestId}/insurance")
    public ApiResponse<InsurancePolicy> saveInsurance(@PathVariable Long contestId,
                                                      @Valid @RequestBody P1Dtos.InsuranceRequest request) {
        return ApiResponse.ok(riskService.saveInsurance(contestId, request));
    }

    @PutMapping("/api/v1/admin/risk/{contestId}/safety")
    public ApiResponse<SafetyPlan> saveSafety(@PathVariable Long contestId,
                                              @Valid @RequestBody P1Dtos.SafetyRequest request) {
        return ApiResponse.ok(riskService.saveSafety(contestId, request));
    }
}
