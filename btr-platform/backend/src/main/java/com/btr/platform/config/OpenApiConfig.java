package com.btr.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI btrOpenApi() {
        return new OpenAPI().info(new Info()
                .title("BTR 社会赛事平台 API")
                .version("0.1.0")
                .description("多租户、赞助、模板、评分表、证书。"));
    }
}
