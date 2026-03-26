package com.orchestra.executor.plugin;

import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestRun;
import com.orchestra.executor.model.ExecutionContext;
import com.orchestra.executor.model.StepExecutionResult;

public interface ProtocolPlugin {
    boolean supports(String channelType);

    StepExecutionResult execute(ScenarioStep step, ExecutionContext context, TestRun run);
}
