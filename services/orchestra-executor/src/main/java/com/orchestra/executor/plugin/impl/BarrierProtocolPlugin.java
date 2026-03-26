package com.orchestra.executor.plugin.impl;

import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestRun;
import com.orchestra.executor.model.ExecutionContext;
import com.orchestra.executor.model.StepExecutionResult;
import com.orchestra.executor.plugin.ProtocolPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BarrierProtocolPlugin implements ProtocolPlugin {

    @Override
    public boolean supports(String channelType) {
        return "CONTROL".equals(channelType);
    }

    @Override
    public StepExecutionResult execute(ScenarioStep step, ExecutionContext context, TestRun run) {
        log.info("Executing BARRIER step: {}. In MVP this is a no-op pass-through.", step.getName());
        // Real barrier logic would check status of tracked steps, but since execution is sequential in MVP, we just pass.
        return StepExecutionResult.empty();
    }
}

