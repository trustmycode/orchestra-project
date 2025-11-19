package com.orchestra.domain.mapper;

import com.orchestra.domain.dto.ProcessModel;
import com.orchestra.domain.model.ProcessVersion;
import org.springframework.stereotype.Component;

@Component
public class ProcessMapper {

    public ProcessModel toDto(ProcessVersion processVersion) {
        ProcessModel dto = new ProcessModel();
        dto.setId(processVersion.getProcess().getId());
        dto.setName(processVersion.getName());
        dto.setSourceType(processVersion.getSourceType());
        dto.setCreatedAt(processVersion.getCreatedAt());
        return dto;
    }
}
