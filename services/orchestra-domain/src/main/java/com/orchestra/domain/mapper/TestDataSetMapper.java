package com.orchestra.domain.mapper;

import com.orchestra.domain.dto.TestDataSetDetail;
import com.orchestra.domain.dto.TestDataSetSummary;
import com.orchestra.domain.model.TestDataSet;
import org.springframework.stereotype.Component;

@Component
public class TestDataSetMapper {

    public TestDataSetSummary toSummary(TestDataSet entity) {
        TestDataSetSummary dto = new TestDataSetSummary();
        dto.setId(entity.getId());
        dto.setScope(entity.getScope());
        if (entity.getSuite() != null) {
            dto.setSuiteId(entity.getSuite().getId());
        }
        if (entity.getScenario() != null) {
            dto.setScenarioId(entity.getScenario().getId());
        }
        dto.setName(entity.getName());
        dto.setTags(entity.getTags());
        dto.setOrigin(entity.getOrigin());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }

    public TestDataSetDetail toDetail(TestDataSet entity) {
        TestDataSetDetail dto = new TestDataSetDetail();
        dto.setId(entity.getId());
        dto.setScope(entity.getScope());
        if (entity.getSuite() != null) {
            dto.setSuiteId(entity.getSuite().getId());
        }
        if (entity.getScenario() != null) {
            dto.setScenarioId(entity.getScenario().getId());
        }
        dto.setName(entity.getName());
        dto.setTags(entity.getTags());
        dto.setOrigin(entity.getOrigin());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setDescription(entity.getDescription());
        dto.setData(entity.getData());
        return dto;
    }

    public TestDataSet toEntity(TestDataSetDetail dto) {
        TestDataSet entity = new TestDataSet();
        entity.setId(dto.getId());
        updateEntityFromDto(entity, dto);
        return entity;
    }

    public void updateEntityFromDto(TestDataSet entity, TestDataSetDetail dto) {
        entity.setScope(dto.getScope());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setTags(dto.getTags());
        entity.setOrigin(dto.getOrigin());
        entity.setData(dto.getData());
    }
}
