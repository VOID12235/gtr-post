package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.MemberDtos;
import com.btr.platform.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/api/v1/admin/members")
    public ApiResponse<List<MemberDtos.MemberView>> list() {
        return ApiResponse.ok(memberService.list());
    }

    @PostMapping("/api/v1/admin/members")
    public ApiResponse<MemberDtos.MemberView> create(@Valid @RequestBody MemberDtos.CreateMemberRequest request) {
        return ApiResponse.ok(memberService.create(request));
    }

    @PutMapping("/api/v1/admin/members/{id}/role")
    public ApiResponse<MemberDtos.MemberView> role(@PathVariable Long id,
                                                   @Valid @RequestBody MemberDtos.RoleRequest request) {
        return ApiResponse.ok(memberService.updateRole(id, request.role()));
    }

    @PutMapping("/api/v1/admin/members/{id}/status")
    public ApiResponse<MemberDtos.MemberView> status(@PathVariable Long id,
                                                     @Valid @RequestBody MemberDtos.StatusRequest request) {
        return ApiResponse.ok(memberService.updateStatus(id, request.status()));
    }
}
