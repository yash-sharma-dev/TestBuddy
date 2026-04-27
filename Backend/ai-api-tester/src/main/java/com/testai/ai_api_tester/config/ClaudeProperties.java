package com.testai.ai_api_tester.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "claude.pricing")
public class ClaudeProperties {
    private BigDecimal inputPerMillion = new BigDecimal("1.0");
    private BigDecimal outputPerMillion = new BigDecimal("5.0");
    private BigDecimal cacheReadPerMillion = new BigDecimal("0.1");
    private BigDecimal cacheCreationPerMillion = new BigDecimal("1.25");
}
