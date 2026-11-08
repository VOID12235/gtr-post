package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.entity.PaymentOrder;
import com.btr.platform.security.SecurityUtils;
import com.btr.platform.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/api/v1/payments/mine")
    public ApiResponse<List<PaymentOrder>> mine() {
        return ApiResponse.ok(paymentService.mine(SecurityUtils.current().userId()));
    }

    @GetMapping("/api/v1/admin/payments")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<PaymentOrder>> list(@RequestParam Long contestId) {
        return ApiResponse.ok(paymentService.listByContest(contestId));
    }

    @PostMapping("/api/v1/admin/payments/{id}/paid")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<PaymentOrder> paid(@PathVariable Long id,
                                          @RequestBody(required = false) Map<String, String> body) {
        String reference = body == null ? null : body.get("reference");
        return ApiResponse.ok(paymentService.markPaid(id, reference));
    }

    @PostMapping("/api/v1/admin/payments/{id}/refund")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<PaymentOrder> refund(@PathVariable Long id) {
        return ApiResponse.ok(paymentService.refund(id));
    }
}
