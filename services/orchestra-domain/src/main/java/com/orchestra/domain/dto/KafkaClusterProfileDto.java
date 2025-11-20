package com.orchestra.domain.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class KafkaClusterProfileDto {
    private UUID id;
    private String name;
    private String bootstrapServers;
    private String securityProtocol;
    private String saslMechanism;
    private String username;
    private String password;
}