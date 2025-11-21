package com.orchestra.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class SuiteRunDetail extends SuiteRunSummary {
    private List<TestRunSummary> testRuns;
    private Map<String, Object> context;
}

