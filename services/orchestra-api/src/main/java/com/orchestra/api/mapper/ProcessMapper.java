package com.orchestra.api.mapper;

import com.orchestra.api.dto.ProcessModel;
import com.orchestra.api.model.ProcessVersion;
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
