package com.orchestra.domain.mapper;

import com.orchestra.domain.dto.ProtocolSpecSummary;
import com.orchestra.domain.model.ProtocolSpec;
import org.springframework.stereotype.Component;

@Component
public class ProtocolSpecMapper {

    public ProtocolSpecSummary toDto(ProtocolSpec protocolSpec) {
        ProtocolSpecSummary dto = new ProtocolSpecSummary();
        dto.setId(protocolSpec.getId());
        dto.setProtocolId(protocolSpec.getProtocolId());
        dto.setServiceName(protocolSpec.getServiceName());
        dto.setVersion(protocolSpec.getVersion());
        dto.setCreatedAt(protocolSpec.getCreatedAt());
        return dto;
    }
}
