package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Sponsor;
import com.btr.platform.security.SecurityUtils;
import com.btr.platform.service.SponsorService;
import jakarta.servlet.http.HttpServletRequest;
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
public class SponsorController {

    private final SponsorService sponsorService;

    public SponsorController(SponsorService sponsorService) {
        this.sponsorService = sponsorService;
    }

    @GetMapping("/api/v1/platform/sponsors")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<List<Sponsor>> sponsors() {
        return ApiResponse.ok(sponsorService.sponsors());
    }

    @PostMapping("/api/v1/platform/sponsors")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<Sponsor> createSponsor(@Valid @RequestBody P1Dtos.SponsorRequest request) {
        return ApiResponse.ok(sponsorService.createSponsor(request));
    }

    @GetMapping("/api/v1/platform/campaigns")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<List<P1Dtos.CampaignView>> campaigns() {
        return ApiResponse.ok(sponsorService.campaigns());
    }

    @PostMapping("/api/v1/platform/campaigns")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<P1Dtos.CampaignView> createCampaign(@Valid @RequestBody P1Dtos.CampaignRequest request) {
        return ApiResponse.ok(sponsorService.createCampaign(request));
    }

    @PostMapping("/api/v1/platform/campaigns/{id}/approve")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<P1Dtos.CampaignView> approve(@PathVariable Long id) {
        return ApiResponse.ok(sponsorService.approve(id));
    }

    @GetMapping("/api/v1/public/placements")
    public ApiResponse<List<P1Dtos.PublicPlacementView>> placements(@RequestParam String slot,
                                                                   @RequestParam(required = false) Long contestId,
                                                                   @RequestParam(defaultValue = "demo") String tenantCode) {
        return ApiResponse.ok(sponsorService.publicPlacements(slot, contestId, tenantCode));
    }

    @PostMapping("/api/v1/public/ad/impression")
    public ApiResponse<Void> impression(@RequestBody P1Dtos.AdEventRequest request, HttpServletRequest servletRequest) {
        return record(request, servletRequest, "IMPRESSION");
    }

    @PostMapping("/api/v1/public/ad/click")
    public ApiResponse<Void> click(@RequestBody P1Dtos.AdEventRequest request, HttpServletRequest servletRequest) {
        return record(request, servletRequest, "CLICK");
    }

    private ApiResponse<Void> record(P1Dtos.AdEventRequest request, HttpServletRequest servletRequest, String type) {
        Long userId = null;
        try {
            userId = SecurityUtils.current().userId();
        } catch (RuntimeException ignored) {
            // anonymous impression/click is allowed
        }
        sponsorService.recordEvent(
                new P1Dtos.AdEventRequest(request.campaignId(), request.placementId(), type),
                userId,
                servletRequest.getRemoteAddr(),
                servletRequest.getHeader("User-Agent")
        );
        return ApiResponse.ok();
    }
}
