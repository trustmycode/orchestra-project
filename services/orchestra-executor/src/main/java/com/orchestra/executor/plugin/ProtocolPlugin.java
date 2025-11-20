package com.orchestra.executor.plugin;

import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestRun;
import com.orchestra.executor.model.ExecutionContext;

public interface ProtocolPlugin {
    boolean supports(String channelType);

    void execute(ScenarioStep step, ExecutionContext context, TestRun run);
}
