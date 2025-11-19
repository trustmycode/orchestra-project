package com.orchestra.executor.plugin.impl;

import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.executor.model.ExecutionContext;
import com.orchestra.executor.plugin.ProtocolPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HttpProtocolPlugin implements ProtocolPlugin {

    @Override
    public boolean supports(String channelType) {
        return "HTTP_REST".equals(channelType);
    }

    @Override
    public void execute(ScenarioStep step, ExecutionContext context) {
        log.info("Executing HTTP step: {} (Alias: {})", step.getName(), step.getAlias());
        // TODO: Implement actual HTTP call using RestTemplate or WebClient
        try {
            Thread.sleep(100); // Simulate network latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
