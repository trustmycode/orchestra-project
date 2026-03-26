package com.orchestra.api.service;

import com.orchestra.domain.model.graph.ControlFlowGraph;
import com.orchestra.domain.model.graph.GraphEdge;
import com.orchestra.domain.model.graph.GraphNode;
import com.orchestra.domain.model.graph.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BpmnGraphParser {

    public ControlFlowGraph parse(String bpmnContent) {
        ControlFlowGraph graph = new ControlFlowGraph();
        try {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(
                    new ByteArrayInputStream(bpmnContent.getBytes(StandardCharsets.UTF_8)));

            // Map FlowNode ID to Lane Name
            Map<String, String> nodeLaneMap = new HashMap<>();
            Collection<Lane> lanes = modelInstance.getModelElementsByType(Lane.class);
            for (Lane lane : lanes) {
                String laneName = lane.getName() != null ? lane.getName() : lane.getId();
                lane.getFlowNodeRefs().forEach(node -> nodeLaneMap.put(node.getId(), laneName));
            }

            Collection<FlowNode> flowNodes = modelInstance.getModelElementsByType(FlowNode.class);
            for (FlowNode fn : flowNodes) {
                GraphNode node = new GraphNode();
                node.setId(fn.getId());
                node.setName(fn.getName());
                node.setType(determineType(fn));

                Map<String, Object> meta = new HashMap<>();
                String doc = fn.getDocumentations().stream()
                        .map(Documentation::getTextContent)
                        .collect(Collectors.joining("\n"));
                if (!doc.isEmpty()) {
                    meta.put("description", doc);
                }
                if (nodeLaneMap.containsKey(fn.getId())) {
                    meta.put("lane", nodeLaneMap.get(fn.getId()));
                }

                node.setMetadata(meta);
                graph.addNode(node);
            }

            Collection<SequenceFlow> sequenceFlows = modelInstance.getModelElementsByType(SequenceFlow.class);
            for (SequenceFlow sf : sequenceFlows) {
                GraphEdge edge = new GraphEdge();
                edge.setId(sf.getId());
                edge.setSourceId(sf.getSource().getId());
                edge.setTargetId(sf.getTarget().getId());

                if (sf.getConditionExpression() != null) {
                    edge.setConditionExpression(sf.getConditionExpression().getTextContent());
                }
                graph.addEdge(edge);
            }

        } catch (Exception e) {
            log.error("Failed to parse BPMN content", e);
            throw new RuntimeException("BPMN parsing failed", e);
        }
        return graph;
    }

    private NodeType determineType(FlowNode fn) {
        // 1. События
        if (fn instanceof StartEvent) {
            return NodeType.START;
        } else if (fn instanceof EndEvent) {
            return NodeType.END;
        } else if (fn instanceof ExclusiveGateway) {
            return NodeType.EXCLUSIVE_GATEWAY;
        } else if (fn instanceof ParallelGateway) {
            return NodeType.PARALLEL_GATEWAY;
        } else if (fn instanceof InclusiveGateway) {
            return NodeType.INCLUSIVE_GATEWAY;
        } else if (fn instanceof Activity) {
            return NodeType.ACTION;
        }
        return NodeType.OTHER;
    }
}
