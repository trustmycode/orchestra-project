package com.orchestra.domain.model.graph;

import lombok.Data;
import java.util.Map;

@Data
public class GraphNode {
    private String id;
    private String name;
    private NodeType type;
    private Map<String, Object> metadata;
}

