package com.orchestra.api.service.puml;

import com.orchestra.domain.model.graph.ControlFlowGraph;
import com.orchestra.domain.model.graph.GraphEdge;
import com.orchestra.domain.model.graph.GraphNode;
import com.orchestra.domain.model.graph.NodeType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlantUmlGraphConverter {

    public ControlFlowGraph convert(PumlDocument doc) {
        ControlFlowGraph graph = new ControlFlowGraph();
        
        // Create Start Node
        GraphNode startNode = createNode("start", "Start", NodeType.START);
        graph.addNode(startNode);

        ConversionContext ctx = new ConversionContext(graph, startNode);
        
        processElements(doc.elements(), ctx);

        // Create End Node and connect last node
        GraphNode endNode = createNode("end", "End", NodeType.END);
        graph.addNode(endNode);
        createEdge(graph, ctx.lastNode.getId(), endNode.getId(), null);

        return graph;
    }

    private void processElements(List<PumlElement> elements, ConversionContext ctx) {
        for (PumlElement el : elements) {
            if (el instanceof PumlMessage msg) {
                processMessage(msg, ctx);
            } else if (el instanceof PumlBlock block) {
                processBlock(block, ctx);
            }
        }
    }

    private void processMessage(PumlMessage msg, ConversionContext ctx) {
        String nodeId = "act_" + UUID.randomUUID().toString().substring(0, 8);
        GraphNode node = createNode(nodeId, msg.text(), NodeType.ACTION);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("source", msg.source());
        meta.put("target", msg.target());
        meta.put("description", msg.text());
        meta.put("lane", msg.target());
        node.setMetadata(meta);

        ctx.graph.addNode(node);
        createEdge(ctx.graph, ctx.lastNode.getId(), nodeId, null);
        ctx.lastNode = node;
    }

    private void processBlock(PumlBlock block, ConversionContext ctx) {
        switch (block.type()) {
            case ALT, OPT -> processAltOpt(block, ctx);
            case LOOP -> processLoop(block, ctx);
            case PAR -> processPar(block, ctx);
        }
    }

    private void processAltOpt(PumlBlock block, ConversionContext ctx) {
        // Split Gateway
        String splitId = "split_" + UUID.randomUUID().toString().substring(0, 8);
        GraphNode splitNode = createNode(splitId, "Decision: " + block.condition(), NodeType.EXCLUSIVE_GATEWAY);
        ctx.graph.addNode(splitNode);
        createEdge(ctx.graph, ctx.lastNode.getId(), splitId, null);

        // Join Gateway
        String joinId = "join_" + UUID.randomUUID().toString().substring(0, 8);
        GraphNode joinNode = createNode(joinId, "Merge", NodeType.EXCLUSIVE_GATEWAY);
        ctx.graph.addNode(joinNode);

        // Process branches
        // Main branch (the 'if' part)
        ConversionContext branchCtx = new ConversionContext(ctx.graph, splitNode);
        processElements(block.children(), branchCtx);
        createEdge(ctx.graph, branchCtx.lastNode.getId(), joinId, block.condition());

        // Else branches
        for (PumlBlock.ElseBlock elseBlock : block.elseBlocks()) {
            ConversionContext elseCtx = new ConversionContext(ctx.graph, splitNode);
            processElements(elseBlock.children(), elseCtx);
            createEdge(ctx.graph, elseCtx.lastNode.getId(), joinId, elseBlock.condition());
        }

        // If OPT, add direct edge from split to join (empty else)
        if (block.type() == PumlBlockType.OPT) {
            createEdge(ctx.graph, splitId, joinId, "else");
        }

        ctx.lastNode = joinNode;
    }

    private void processLoop(PumlBlock block, ConversionContext ctx) {
        // Merge Gateway (entry point for loop back)
        String mergeId = "loop_merge_" + UUID.randomUUID().toString().substring(0, 8);
        GraphNode mergeNode = createNode(mergeId, "Loop Start", NodeType.EXCLUSIVE_GATEWAY);
        ctx.graph.addNode(mergeNode);
        createEdge(ctx.graph, ctx.lastNode.getId(), mergeId, null);

        // Body
        ConversionContext bodyCtx = new ConversionContext(ctx.graph, mergeNode);
        processElements(block.children(), bodyCtx);

        // Loop back edge
        createEdge(ctx.graph, bodyCtx.lastNode.getId(), mergeId, "repeat");

        // Exit node (after loop)
        // In simple graph conversion, we assume loop can exit. 
        // We connect mergeNode to a new node that represents "after loop"
        // But strictly, we need a decision gateway. For simplicity in MVP, we assume loop runs N times.
        // Let's just set lastNode to mergeNode, implying flow continues from there (or breaks).
        // Better: Create an exit gateway.
        
        ctx.lastNode = mergeNode;
    }

    private void processPar(PumlBlock block, ConversionContext ctx) {
        // Fork
        String forkId = "fork_" + UUID.randomUUID().toString().substring(0, 8);
        GraphNode forkNode = createNode(forkId, "Parallel Split", NodeType.PARALLEL_GATEWAY);
        ctx.graph.addNode(forkNode);
        createEdge(ctx.graph, ctx.lastNode.getId(), forkId, null);

        // Join
        String joinId = "join_" + UUID.randomUUID().toString().substring(0, 8);
        GraphNode joinNode = createNode(joinId, "Parallel Join", NodeType.PARALLEL_GATEWAY);
        ctx.graph.addNode(joinNode);

        // Main branch
        ConversionContext mainCtx = new ConversionContext(ctx.graph, forkNode);
        processElements(block.children(), mainCtx);
        createEdge(ctx.graph, mainCtx.lastNode.getId(), joinId, null);

        // Else branches (parallel sections)
        for (PumlBlock.ElseBlock elseBlock : block.elseBlocks()) {
            ConversionContext elseCtx = new ConversionContext(ctx.graph, forkNode);
            processElements(elseBlock.children(), elseCtx);
            createEdge(ctx.graph, elseCtx.lastNode.getId(), joinId, null);
        }

        ctx.lastNode = joinNode;
    }

    private GraphNode createNode(String id, String name, NodeType type) {
        GraphNode node = new GraphNode();
        node.setId(id);
        node.setName(name);
        node.setType(type);
        return node;
    }

    private void createEdge(ControlFlowGraph graph, String source, String target, String condition) {
        GraphEdge edge = new GraphEdge();
        edge.setId("flow_" + UUID.randomUUID().toString().substring(0, 8));
        edge.setSourceId(source);
        edge.setTargetId(target);
        edge.setConditionExpression(condition);
        graph.addEdge(edge);
    }

    private static class ConversionContext {
        ControlFlowGraph graph;
        GraphNode lastNode;

        public ConversionContext(ControlFlowGraph graph, GraphNode lastNode) {
            this.graph = graph;
            this.lastNode = lastNode;
        }
    }
}

