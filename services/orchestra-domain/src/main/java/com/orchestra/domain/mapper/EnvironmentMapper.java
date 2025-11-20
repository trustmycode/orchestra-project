package com.orchestra.domain.mapper;

import com.orchestra.domain.dto.DbConnectionProfileDto;
import com.orchestra.domain.dto.EnvironmentDto;
import com.orchestra.domain.dto.KafkaClusterProfileDto;
import com.orchestra.domain.model.DbConnectionProfile;
import com.orchestra.domain.model.Environment;
import com.orchestra.domain.model.KafkaClusterProfile;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentMapper {

    public DbConnectionProfileDto toDto(DbConnectionProfile entity) {
        if (entity == null)
            return null;
        DbConnectionProfileDto dto = new DbConnectionProfileDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setJdbcUrl(entity.getJdbcUrl());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        return dto;
    }

    public DbConnectionProfile toEntity(DbConnectionProfileDto dto) {
        if (dto == null)
            return null;
        DbConnectionProfile entity = new DbConnectionProfile();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setJdbcUrl(dto.getJdbcUrl());
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        return entity;
    }

    public KafkaClusterProfileDto toDto(KafkaClusterProfile entity) {
        if (entity == null)
            return null;
        KafkaClusterProfileDto dto = new KafkaClusterProfileDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBootstrapServers(entity.getBootstrapServers());
        dto.setSecurityProtocol(entity.getSecurityProtocol());
        dto.setSaslMechanism(entity.getSaslMechanism());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        return dto;
    }

    public KafkaClusterProfile toEntity(KafkaClusterProfileDto dto) {
        if (dto == null)
            return null;
        KafkaClusterProfile entity = new KafkaClusterProfile();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setBootstrapServers(dto.getBootstrapServers());
        entity.setSecurityProtocol(dto.getSecurityProtocol());
        entity.setSaslMechanism(dto.getSaslMechanism());
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        return entity;
    }

    public EnvironmentDto toDto(Environment entity) {
        if (entity == null)
            return null;
        EnvironmentDto dto = new EnvironmentDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setProfileMappings(entity.getProfileMappings());
        return dto;
    }

    public Environment toEntity(EnvironmentDto dto) {
        if (dto == null)
            return null;
        Environment entity = new Environment();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setProfileMappings(dto.getProfileMappings());
        return entity;
    }
}