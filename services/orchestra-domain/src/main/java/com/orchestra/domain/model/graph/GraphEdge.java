package com.orchestra.domain.model.graph;

import lombok.Data;

@Data
public class GraphEdge {
    private String id;
    private String sourceId;
    private String targetId;
    private String conditionExpression;
}

