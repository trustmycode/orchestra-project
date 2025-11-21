package com.orchestra.domain.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class DataResolverDto {
    private UUID id;
    private String entityName;
    private String dataSource;
    private String mapping;
}

