package com.orchestra.domain.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ProtocolSpecSummary {
    private UUID id;
    private String protocolId;
    private String serviceName;
    private String version;
    private OffsetDateTime createdAt;
}
