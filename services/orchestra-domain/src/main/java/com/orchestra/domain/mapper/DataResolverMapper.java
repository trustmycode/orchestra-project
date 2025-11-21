package com.orchestra.domain.mapper;

import com.orchestra.domain.dto.DataResolverDto;
import com.orchestra.domain.model.DataResolver;
import org.springframework.stereotype.Component;

@Component
public class DataResolverMapper {

    public DataResolverDto toDto(DataResolver entity) {
        if (entity == null) return null;
        DataResolverDto dto = new DataResolverDto();
        dto.setId(entity.getId());
        dto.setEntityName(entity.getEntityName());
        dto.setDataSource(entity.getDataSource());
        dto.setMapping(entity.getMapping());
        return dto;
    }

    public DataResolver toEntity(DataResolverDto dto) {
        if (dto == null) return null;
        DataResolver entity = new DataResolver();
        entity.setId(dto.getId());
        entity.setEntityName(dto.getEntityName());
        entity.setDataSource(dto.getDataSource());
        entity.setMapping(dto.getMapping());
        return entity;
    }
}

