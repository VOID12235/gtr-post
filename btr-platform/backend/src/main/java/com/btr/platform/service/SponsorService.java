package com.btr.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btr.platform.common.BizException;
import com.btr.platform.common.ErrorCode;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.Campaign;
import com.btr.platform.entity.CampaignPlacement;
import com.btr.platform.entity.Sponsor;
import com.btr.platform.entity.AdEvent;
import com.btr.platform.mapper.AdEventMapper;
import com.btr.platform.mapper.CampaignMapper;
import com.btr.platform.mapper.CampaignPlacementMapper;
import com.btr.platform.mapper.SponsorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SponsorService {

    private final SponsorMapper sponsorMapper;
    private final CampaignMapper campaignMapper;
    private final CampaignPlacementMapper placementMapper;
    private final AdEventMapper adEventMapper;
    private final TenantService tenantService;

    public SponsorService(SponsorMapper sponsorMapper,
                          CampaignMapper campaignMapper,
                          CampaignPlacementMapper placementMapper,
                          AdEventMapper adEventMapper,
                          TenantService tenantService) {
        this.sponsorMapper = sponsorMapper;
        this.campaignMapper = campaignMapper;
        this.placementMapper = placementMapper;
        this.adEventMapper = adEventMapper;
        this.tenantService = tenantService;
    }

    public List<Sponsor> sponsors() {
        return sponsorMapper.selectList(new QueryWrapper<Sponsor>().orderByDesc("id"));
    }

    @Transactional
    public Sponsor createSponsor(P1Dtos.SponsorRequest request) {
        Sponsor sponsor = new Sponsor();
        sponsor.name = request.name();
        sponsor.industry = request.industry();
        sponsor.contactName = request.contactName();
        sponsor.contactEmail = request.contactEmail();
        sponsor.status = "ACTIVE";
        sponsorMapper.insert(sponsor);
        return sponsor;
    }

    public List<P1Dtos.CampaignView> campaigns() {
        return campaignMapper.selectList(new QueryWrapper<Campaign>().orderByDesc("id"))
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public P1Dtos.CampaignView createCampaign(P1Dtos.CampaignRequest request) {
        Sponsor sponsor = sponsorMapper.selectById(request.sponsorId());
        if (sponsor == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "赞助商不存在");
        }
        Campaign campaign = new Campaign();
        campaign.sponsorId = request.sponsorId();
        campaign.tenantId = request.tenantId();
        campaign.name = request.name();
        campaign.type = request.type();
        campaign.budget = request.budget();
        campaign.creativeText = request.creativeText();
        campaign.creativeUrl = request.creativeUrl();
        campaign.status = "DRAFT";
        campaign.startAt = request.startAt();
        campaign.endAt = request.endAt();
        campaignMapper.insert(campaign);

        if (request.slot() != null && !request.slot().isBlank()) {
            CampaignPlacement placement = new CampaignPlacement();
            placement.campaignId = campaign.id;
            placement.slot = request.slot();
            placement.contestId = request.contestId();
            placement.weight = 100;
            placement.startAt = request.startAt();
            placement.endAt = request.endAt();
            placementMapper.insert(placement);
        }
        return toView(campaign);
    }

    @Transactional
    public P1Dtos.CampaignView approve(Long campaignId) {
        Campaign campaign = campaignMapper.selectById(campaignId);
        if (campaign == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "赞助活动不存在");
        }
        campaign.status = "APPROVED";
        campaignMapper.updateById(campaign);
        return toView(campaign);
    }

    public List<P1Dtos.PublicPlacementView> publicPlacements(String slot, Long contestId, String tenantCode) {
        Long tenantId = tenantService.requireByCode(tenantCode).getId();
        LocalDateTime now = LocalDateTime.now();

        List<P1Dtos.PublicPlacementView> result = new ArrayList<>();
        List<Campaign> campaigns = campaignMapper.selectList(new QueryWrapper<Campaign>()
                .eq("status", "APPROVED")
                .and(w -> w.isNull("tenant_id").or().eq("tenant_id", tenantId))
                .and(w -> w.isNull("start_at").or().le("start_at", now))
                .and(w -> w.isNull("end_at").or().ge("end_at", now))
                .orderByDesc("id"));
        for (Campaign campaign : campaigns) {
            if (result.size() >= 3) {
                break;
            }
            List<CampaignPlacement> placements = placementMapper.selectList(new QueryWrapper<CampaignPlacement>()
                    .eq("campaign_id", campaign.id)
                    .eq("slot", slot)
                    .and(w -> w.isNull("contest_id").or().eq("contest_id", contestId)));
            Sponsor sponsor = sponsorMapper.selectById(campaign.sponsorId);
            for (CampaignPlacement placement : placements) {
                result.add(new P1Dtos.PublicPlacementView(
                        campaign.id,
                        placement.id,
                        placement.slot,
                        placement.contestId,
                        sponsor == null ? "赞助商" : sponsor.name,
                        campaign.creativeText == null ? campaign.name : campaign.creativeText,
                        campaign.creativeUrl
                ));
                if (result.size() >= 3) {
                    break;
                }
            }
        }
        return result;
    }

    public void recordEvent(P1Dtos.AdEventRequest request, Long userId, String ip, String userAgent) {
        Campaign campaign = campaignMapper.selectById(request.campaignId());
        if (campaign == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "赞助活动不存在");
        }
        AdEvent event = new AdEvent();
        event.campaignId = request.campaignId();
        event.placementId = request.placementId();
        event.eventType = request.eventType() == null ? "IMPRESSION" : request.eventType();
        event.userId = userId;
        event.ip = ip;
        event.userAgent = userAgent;
        adEventMapper.insert(event);
    }

    public P1Dtos.CampaignView toView(Campaign campaign) {
        Sponsor sponsor = sponsorMapper.selectById(campaign.sponsorId);
        List<P1Dtos.PlacementView> placements = placementMapper.selectList(new QueryWrapper<CampaignPlacement>()
                        .eq("campaign_id", campaign.id))
                .stream()
                .map(p -> new P1Dtos.PlacementView(p.id, p.slot, p.contestId, p.weight))
                .toList();
        return new P1Dtos.CampaignView(
                campaign.id,
                campaign.sponsorId,
                sponsor == null ? null : sponsor.name,
                campaign.tenantId,
                campaign.name,
                campaign.type,
                campaign.budget,
                campaign.status,
                campaign.creativeText,
                campaign.creativeUrl,
                campaign.startAt,
                campaign.endAt,
                placements
        );
    }
}
