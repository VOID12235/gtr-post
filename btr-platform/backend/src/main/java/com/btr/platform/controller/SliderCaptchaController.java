package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.dto.AuthDtos;
import com.btr.platform.service.SliderCaptchaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/captcha")
public class SliderCaptchaController {

    private final SliderCaptchaService captchaService;

    public SliderCaptchaController(SliderCaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping("/slider")
    public ApiResponse<Map<String, String>> create() {
        return ApiResponse.ok(captchaService.create());
    }

    @PostMapping("/slider/verify")
    public ApiResponse<Map<String, String>> verify(@Valid @RequestBody AuthDtos.SliderVerifyRequest request) {
        String ticket = captchaService.verify(request.challengeId(), request.position(), request.duration());
        return ApiResponse.ok(Map.of("ticket", ticket));
    }
}
