package com.orchestra.domain.mapper;

import com.orchestra.domain.dto.SuiteRunDetail;
import com.orchestra.domain.dto.SuiteRunSummary;
import com.orchestra.domain.model.SuiteRun;
import com.orchestra.domain.model.TestRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SuiteRunMapper {

    private final TestRunMapper testRunMapper;

    public SuiteRunSummary toSummary(SuiteRun entity) {
        SuiteRunSummary dto = new SuiteRunSummary();
        dto.setId(entity.getId());
        if (entity.getSuite() != null) {
            dto.setSuiteId(entity.getSuite().getId());
            dto.setSuiteName(entity.getSuite().getName());
        }
        dto.setStatus(entity.getStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setFinishedAt(entity.getFinishedAt());
        return dto;
    }

    public SuiteRunDetail toDetail(SuiteRun entity, List<TestRun> testRuns) {
        SuiteRunDetail dto = new SuiteRunDetail();
        dto.setId(entity.getId());
        if (entity.getSuite() != null) {
            dto.setSuiteId(entity.getSuite().getId());
            dto.setSuiteName(entity.getSuite().getName());
        }
        dto.setStatus(entity.getStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setFinishedAt(entity.getFinishedAt());
        dto.setContext(entity.getContext());
        dto.setTestRuns(testRuns.stream().map(testRunMapper::toSummary).collect(Collectors.toList()));
        return dto;
    }
}

