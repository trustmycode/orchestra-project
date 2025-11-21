package com.orchestra.domain.mapper;

import com.orchestra.domain.dto.ScenarioStepDto;
import com.orchestra.domain.dto.TestScenarioDetail;
import com.orchestra.domain.dto.TestScenarioSummary;
import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestScenario;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TestScenarioMapper {

    public TestScenarioSummary toSummary(TestScenario entity) {
        TestScenarioSummary dto = new TestScenarioSummary();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getSuite() != null) {
            dto.setSuiteId(entity.getSuite().getId());
            if (entity.getSuite().getProcess() != null) {
                dto.setProcessId(entity.getSuite().getProcess().getId());
            }
            if (entity.getSuite().getProcessVersion() != null) {
                dto.setProcessVersion(entity.getSuite().getProcessVersion().getVersion());
            }
        }
        dto.setKey(entity.getKey());
        dto.setActive(entity.isActive());
        dto.setVersion(entity.getVersion());
        dto.setStatus(entity.getStatus());
        dto.setTags(entity.getTags());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public TestScenarioDetail toDetail(TestScenario entity) {
        TestScenarioDetail dto = new TestScenarioDetail();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getSuite() != null) {
            dto.setSuiteId(entity.getSuite().getId());
            if (entity.getSuite().getProcess() != null) {
                dto.setProcessId(entity.getSuite().getProcess().getId());
            }
            if (entity.getSuite().getProcessVersion() != null) {
                dto.setProcessVersion(entity.getSuite().getProcessVersion().getVersion());
            }
        }
        dto.setKey(entity.getKey());
        dto.setActive(entity.isActive());
        dto.setVersion(entity.getVersion());
        dto.setStatus(entity.getStatus());
        dto.setTags(entity.getTags());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setSteps(entity.getSteps().stream().map(this::toDto).collect(Collectors.toList()));
        dto.setDependsOn(entity.getDependsOn());
        return dto;
    }

    public void updateEntityFromDto(TestScenario entity, TestScenarioDetail dto) {
        entity.setName(dto.getName());
        entity.setKey(dto.getKey());
        entity.setActive(dto.isActive());
        entity.setVersion(dto.getVersion());
        entity.setStatus(dto.getStatus());
        entity.setTags(dto.getTags());
        entity.setDependsOn(dto.getDependsOn());

        entity.getSteps().clear();
        if (dto.getSteps() != null) {
            dto.getSteps().forEach(stepDto -> {
                ScenarioStep step = toEntity(stepDto);
                step.setScenario(entity);
                entity.getSteps().add(step);
            });
        }
    }

    public TestScenario toEntity(TestScenarioDetail dto) {
        TestScenario entity = new TestScenario();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setKey(dto.getKey());
        entity.setActive(dto.isActive());
        entity.setVersion(dto.getVersion());
        entity.setStatus(dto.getStatus());
        entity.setTags(dto.getTags());
        entity.setDependsOn(dto.getDependsOn());
        if (dto.getSteps() != null) {
            entity.setSteps(dto.getSteps().stream().map(stepDto -> {
                ScenarioStep step = toEntity(stepDto);
                step.setScenario(entity);
                return step;
            }).collect(Collectors.toList()));
        }
        return entity;
    }

    private ScenarioStepDto toDto(ScenarioStep entity) {
        ScenarioStepDto dto = new ScenarioStepDto();
        dto.setId(entity.getId());
        dto.setOrderIndex(entity.getOrderIndex());
        dto.setAlias(entity.getAlias());
        dto.setName(entity.getName());
        dto.setKind(entity.getKind());
        dto.setChannelType(entity.getChannelType());
        dto.setEndpointRef(entity.getEndpointRef());
        dto.setExportAs(entity.getExportAs());
        dto.setAction(entity.getAction());
        dto.setExpectations(entity.getExpectations());
        return dto;
    }

    public ScenarioStep toEntity(ScenarioStepDto dto) {
        ScenarioStep entity = new ScenarioStep();
        entity.setId(dto.getId() != null ? dto.getId() : java.util.UUID.randomUUID());
        entity.setOrderIndex(dto.getOrderIndex());
        entity.setAlias(dto.getAlias());
        entity.setName(dto.getName());
        entity.setKind(dto.getKind());
        entity.setChannelType(dto.getChannelType());
        entity.setEndpointRef(dto.getEndpointRef());
        entity.setExportAs(dto.getExportAs());
        entity.setAction(dto.getAction());
        entity.setExpectations(dto.getExpectations());
        return entity;
    }
}
