package com.orchestra.domain.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class DbConnectionProfileDto {
    private UUID id;
    private String name;
    private String jdbcUrl;
    private String username;
    private String password;
}