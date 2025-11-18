package com.orchestra.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestDataSetDetail extends TestDataSetSummary {
    private String description;
    private Map<String, Object> data;
}
