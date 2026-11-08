package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.P1Dtos;
import com.btr.platform.entity.JudgeScore;
import com.btr.platform.security.SecurityUtils;
import com.btr.platform.service.JudgeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JudgeController {

    private final JudgeService judgeService;

    public JudgeController(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @GetMapping("/api/v1/judge/tasks")
    @PreAuthorize("hasAnyRole('JUDGE','TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<P1Dtos.WorkView>> tasks(@RequestParam Long contestId) {
        return ApiResponse.ok(judgeService.tasks(contestId, SecurityUtils.current().userId()));
    }

    @PostMapping("/api/v1/judge/scores")
    @PreAuthorize("hasAnyRole('JUDGE','TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<JudgeScore> score(@Valid @RequestBody P1Dtos.ScoreRequest request) {
        return ApiResponse.ok(judgeService.score(request, SecurityUtils.current().userId()));
    }

    @GetMapping("/api/v1/admin/scores")
    @PreAuthorize("hasAnyRole('TENANT_OWNER','TENANT_ADMIN','PLATFORM_ADMIN')")
    public ApiResponse<List<JudgeScore>> scores(@RequestParam Long contestId) {
        return ApiResponse.ok(judgeService.scores(contestId));
    }
}
