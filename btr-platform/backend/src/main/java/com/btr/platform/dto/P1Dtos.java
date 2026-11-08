package com.btr.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class P1Dtos {

    private P1Dtos() {
    }

    public record CreateContestRequest(
            @NotBlank String title,
            String summary,
            String cover,
            String mode,
            Boolean feeEnabled,
            BigDecimal feeAmount,
            LocalDateTime signupStart,
            LocalDateTime signupEnd,
            LocalDateTime submitStart,
            LocalDateTime submitEnd,
            LocalDateTime reviewStart,
            LocalDateTime reviewEnd
    ) {
    }

    public record StatusRequest(@NotBlank String status) {
    }

    public record SignupRequest(
            @NotNull Long contestId,
            String teamName,
            String contact
    ) {
    }

    public record WorkRequest(
            @NotNull Long contestId,
            @NotBlank String title,
            String description,
            String videoUrl,
            String fileUrl
    ) {
    }

    public record RubricItemRequest(
            @NotBlank String key,
            @NotBlank String label,
            @NotNull BigDecimal weight,
            BigDecimal min,
            BigDecimal max
    ) {
    }

    public record CreateRubricRequest(
            @NotNull Long contestId,
            @NotBlank String name,
            @NotBlank String mode,
            @NotNull List<RubricItemRequest> items
    ) {
    }

    public record ScoreRequest(
            @NotNull Long workId,
            @NotNull BigDecimal totalScore,
            String comment
    ) {
    }

    public record SponsorRequest(
            @NotBlank String name,
            String industry,
            String contactName,
            String contactEmail
    ) {
    }

    public record CampaignRequest(
            @NotNull Long sponsorId,
            Long tenantId,
            @NotBlank String name,
            @NotBlank String type,
            BigDecimal budget,
            String creativeText,
            String creativeUrl,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String slot,
            Long contestId
    ) {
    }

    public record AdEventRequest(
            @NotNull Long campaignId,
            Long placementId,
            String eventType
    ) {
    }

    public record CertificateRequest(
            @NotNull Long contestId,
            String award
    ) {
    }

    public record PrizeRequest(
            @NotNull BigDecimal totalAmount,
            BigDecimal fundedAmount,
            @NotBlank String status,
            String note
    ) {
    }

    public record InsuranceRequest(
            @NotBlank String type,
            String insurer,
            String policyNo,
            String coverage,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            @NotBlank String status
    ) {
    }

    public record SafetyRequest(
            @NotBlank String level,
            String venue,
            Integer capacity,
            String plan,
            @NotBlank String status
    ) {
    }

    public record ProductRequest(
            @NotBlank String name,
            String description,
            String cover,
            @NotNull BigDecimal price,
            @NotNull Integer stock
    ) {
    }

    public record ProductStatusRequest(@NotBlank String status) {
    }

    public record ShopOrderRequest(
            @NotNull Long productId,
            @NotNull Integer quantity,
            @NotBlank String contactName,
            @NotBlank String contactPhone,
            @NotBlank String address,
            String note
    ) {
    }

    public record ShipRequest(@NotBlank String trackingNo) {
    }

    public record ContestView(
            Long id,
            String title,
            String summary,
            String cover,
            String mode,
            String status,
            Boolean feeEnabled,
            BigDecimal feeAmount,
            LocalDateTime signupStart,
            LocalDateTime signupEnd,
            LocalDateTime submitStart,
            LocalDateTime submitEnd,
            LocalDateTime reviewStart,
            LocalDateTime reviewEnd,
            LocalDateTime publishedAt
    ) {
    }

    public record WorkView(
            Long id,
            Long contestId,
            String title,
            String description,
            String videoUrl,
            String fileUrl,
            String status,
            BigDecimal totalScore,
            String authorNickname
    ) {
    }

    public record RubricItemView(
            Long id,
            String key,
            String label,
            BigDecimal weight,
            BigDecimal min,
            BigDecimal max,
            Integer sortOrder
    ) {
    }

    public record RubricView(
            Long id,
            Long contestId,
            String name,
            String mode,
            String status,
            List<RubricItemView> items
    ) {
    }

    public record SponsorView(
            Long id,
            String name,
            String industry,
            String contactName,
            String contactEmail,
            String status,
            LocalDateTime createdAt
    ) {
    }

    public record PlacementView(
            Long id,
            String slot,
            Long contestId,
            Integer weight
    ) {
    }

    public record CampaignView(
            Long id,
            Long sponsorId,
            String sponsorName,
            Long tenantId,
            String name,
            String type,
            BigDecimal budget,
            String status,
            String creativeText,
            String creativeUrl,
            LocalDateTime startAt,
            LocalDateTime endAt,
            List<PlacementView> placements
    ) {
    }

    public record PublicPlacementView(
            Long campaignId,
            Long placementId,
            String slot,
            Long contestId,
            String sponsorName,
            String text,
            String url
    ) {
    }

    public record CertificateView(
            Long id,
            Long contestId,
            String contestTitle,
            String award,
            String code,
            String status,
            LocalDateTime issuedAt,
            String holder
    ) {
    }

    public record DashboardView(
            long contests,
            long signups,
            long works,
            long certificates,
            long unreadNotifications
    ) {
    }
}
