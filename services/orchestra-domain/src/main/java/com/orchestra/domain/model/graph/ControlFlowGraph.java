package com.orchestra.domain.model.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class ControlFlowGraph {
    private List<GraphNode> nodes = new ArrayList<>();
    private List<GraphEdge> edges = new ArrayList<>();

    @JsonIgnore
    private transient Map<String, GraphNode> nodeMap;
    @JsonIgnore
    private transient Map<String, List<GraphEdge>> outgoingMap;

    public void addNode(GraphNode node) {
        nodes.add(node);
        nodeMap = null; // invalidate cache
    }

    public void addEdge(GraphEdge edge) {
        edges.add(edge);
        outgoingMap = null; // invalidate cache
    }

    public GraphNode getNode(String id) {
        if (nodeMap == null) {
            nodeMap = nodes.stream().collect(Collectors.toMap(GraphNode::getId, Function.identity()));
        }
        return nodeMap.get(id);
    }

    public List<GraphEdge> getOutgoingEdges(String sourceId) {
        if (outgoingMap == null) {
            outgoingMap = edges.stream().collect(Collectors.groupingBy(GraphEdge::getSourceId));
        }
        return outgoingMap.getOrDefault(sourceId, new ArrayList<>());
    }
}

