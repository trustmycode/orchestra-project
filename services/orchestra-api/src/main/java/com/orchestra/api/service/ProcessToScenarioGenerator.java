package com.orchestra.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.api.exception.ImportException;
import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.domain.dto.AiDataTransferRequest;
import com.orchestra.domain.dto.AiDataTransferResponse;
import com.orchestra.domain.dto.AiGenerateScenarioResponse;
import com.orchestra.domain.dto.AiGenerateSuiteResponse;
import com.orchestra.domain.dto.AiJob;
import com.orchestra.domain.dto.ScenarioSuiteDetail;
import com.orchestra.domain.dto.ScenarioSuiteGenerateRequest;
import com.orchestra.domain.mapper.ScenarioSuiteMapper;
import com.orchestra.domain.model.Process;
import com.orchestra.domain.model.ProcessVersion;
import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.ScenarioSuite;
import com.orchestra.domain.model.TestDataSet;
import com.orchestra.domain.model.Tenant;
import com.orchestra.domain.model.TestScenario;
import com.orchestra.api.service.puml.PlantUmlGraphConverter;
import com.orchestra.api.service.puml.PlantUmlParser;
import com.orchestra.api.service.puml.PumlDocument;
import com.orchestra.domain.model.graph.ControlFlowGraph;
import com.orchestra.domain.model.graph.GraphEdge;
import com.orchestra.domain.model.graph.GraphNode;
import com.orchestra.domain.model.graph.NodeType;
import com.orchestra.domain.repository.ProcessRepository;
import com.orchestra.domain.repository.ProcessVersionRepository;
import com.orchestra.domain.repository.ScenarioSuiteRepository;
import com.orchestra.domain.repository.TenantRepository;
import com.orchestra.domain.repository.TestDataSetRepository;
import com.orchestra.domain.repository.TestScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessToScenarioGenerator {

    private final ProcessRepository processRepository;
    private final ProcessVersionRepository processVersionRepository;
    private final ScenarioSuiteRepository scenarioSuiteRepository;
    private final TenantRepository tenantRepository;
    private final ScenarioSuiteMapper scenarioSuiteMapper;
    private final TestScenarioRepository testScenarioRepository;
    private final TestDataSetRepository testDataSetRepository;
    private final ArtifactStorageService artifactStorageService;
    private final BpmnGraphParser bpmnGraphParser;
    private final PlantUmlParser plantUmlParser;
    private final PlantUmlGraphConverter plantUmlGraphConverter;
    private final ObjectMapper objectMapper;
    private final EndpointMatcher endpointMatcher;
    private final AiService aiService;
    private final AiJobService aiJobService;

    @Lazy
    @Autowired
    private ProcessToScenarioGenerator self;

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Transactional
    public ScenarioSuiteDetail generate(ScenarioSuiteGenerateRequest request) {
        log.info("Generating scenario suite from process: {}", request.getProcessId());

        Process process = processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new ResourceNotFoundException("Process not found: " + request.getProcessId()));

        ProcessVersion processVersion;
        if (request.getProcessVersion() != null) {
            processVersion = processVersionRepository.findByProcessAndVersion(process, request.getProcessVersion())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Process version " + request.getProcessVersion() + " not found"));
        } else {
            processVersion = processVersionRepository.findTopByProcessOrderByVersionDesc(process)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No versions found for process: " + request.getProcessId()));
        }

        Tenant tenant = tenantRepository.findById(DEFAULT_TENANT_ID)
                .orElseThrow(() -> new IllegalStateException("Default tenant not found"));

        ScenarioSuite suite = new ScenarioSuite();
        suite.setId(UUID.randomUUID());
        suite.setTenant(tenant);
        suite.setProcess(process);
        suite.setProcessVersion(processVersion);
        suite.setName(request.getName());
        suite.setTags(request.getTags());

        // Synchronous generation implies immediate readiness
        suite.setStatus("DRAFT");

        String mode = request.getGenerationMode() != null ? request.getGenerationMode() : "ALL_PATHS";
        suite.setDescription("Generated from process " + process.getKey() + " version " + processVersion.getVersion()
                + ". Mode: " + mode);

        ScenarioSuite savedSuite = scenarioSuiteRepository.save(suite);

        populateSuite(savedSuite, processVersion, request);

        return scenarioSuiteMapper.toDetail(savedSuite);
    }

    public Map<String, UUID> generateAsync(ScenarioSuiteGenerateRequest request) {
        log.info("Initiating ASYNC scenario suite generation for process: {}", request.getProcessId());

        Process process = processRepository.findById(request.getProcessId())
                .orElseThrow(() -> new ResourceNotFoundException("Process not found: " + request.getProcessId()));

        Tenant tenant = tenantRepository.findById(DEFAULT_TENANT_ID)
                .orElseThrow(() -> new IllegalStateException("Default tenant not found"));

        // Create Job
        AiJob job = aiJobService.createJob();

        // Create Suite Placeholder
        ScenarioSuite suite = new ScenarioSuite();
        suite.setId(UUID.randomUUID());
        suite.setTenant(tenant);
        suite.setProcess(process);
        suite.setName(request.getName());
        suite.setTags(request.getTags());
        suite.setStatus("GENERATING");
        suite.setGenerationJobId(job.getId());
        suite.setDescription("Async generation in progress. Job ID: " + job.getId());

        ScenarioSuite savedSuite = scenarioSuiteRepository.save(suite);

        // Trigger Async Processing
        self.performAsyncGeneration(savedSuite.getId(), request.getProcessVersion(), request, job.getId());

        return Map.of("suiteId", savedSuite.getId(), "jobId", job.getId());
    }

    @Async
    public void performAsyncGeneration(UUID suiteId, Integer processVersionNumber, ScenarioSuiteGenerateRequest request,
            UUID jobId) {
        try {
            // Вызываем транзакционный метод через self-proxy
            self.executeGenerationLogic(suiteId, processVersionNumber, request, jobId);

        } catch (Exception e) {
            log.error("Async suite generation failed for suite {}", suiteId, e);

            // Теперь этот блок выполняется в новой, чистой транзакции (или без нее),
            // так как предыдущая транзакция (если бы она была) не влияет на этот catch.
            scenarioSuiteRepository.findById(suiteId).ifPresent(s -> {
                s.setStatus("FAILED");
                String msg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                s.setDescription("Generation failed: " + (msg.length() > 250 ? msg.substring(0, 250) + "..." : msg));
                scenarioSuiteRepository.save(s);
            });

            aiJobService.fail(jobId, e.getMessage());
        }
    }

    // === ИЗМЕНЕНИЕ 2: Выносим логику в отдельный транзакционный метод ===
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void executeGenerationLogic(UUID suiteId, Integer processVersionNumber, ScenarioSuiteGenerateRequest request,
            UUID jobId) {
        aiJobService.updateProgress(jobId, 10, "Initializing generation...");

        ScenarioSuite suite = scenarioSuiteRepository.findById(suiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Suite not found during async processing"));

        ProcessVersion processVersion;
        if (processVersionNumber != null) {
            processVersion = processVersionRepository.findByProcessAndVersion(suite.getProcess(), processVersionNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Process version not found"));
        } else {
            processVersion = processVersionRepository.findTopByProcessOrderByVersionDesc(suite.getProcess())
                    .orElseThrow(() -> new ResourceNotFoundException("No versions found for process"));
        }

        suite.setProcessVersion(processVersion);

        aiJobService.updateProgress(jobId, 30, "Parsing process graph...");
        populateSuite(suite, processVersion, request);

        // TASK-2024-151: Data Hydration
        boolean hydrationSuccess = true;
        if (request.getEnvironmentId() != null) {
            aiJobService.updateProgress(jobId, 80, "Hydrating test data with AI...");
            hydrationSuccess = hydrateSuiteData(suite, request.getEnvironmentId());
        }

        suite.setStatus("DRAFT");
        String desc = "Generated successfully.";
        if (!hydrationSuccess) {
            desc += " Warning: Data hydration failed. Check logs.";
        }
        suite.setDescription(suite.getDescription().replace("Async generation in progress.", desc));
        scenarioSuiteRepository.save(suite);

        aiJobService.complete(jobId, Map.of("suiteId", suite.getId(), "status", "COMPLETED"));
    }

    private void populateSuite(ScenarioSuite suite, ProcessVersion processVersion,
            ScenarioSuiteGenerateRequest request) {
        log.info(">>> Starting populateSuite for Suite ID: {}", suite.getId()); // LOG

        // 1. Load or Parse Graph
        ControlFlowGraph graph = null;
        if (processVersion.getControlFlowGraph() != null && !processVersion.getControlFlowGraph().isEmpty()) {
            try {
                graph = objectMapper.convertValue(processVersion.getControlFlowGraph(), ControlFlowGraph.class);
                log.info("Graph loaded from DB JSON. Nodes: {}", graph.getNodes().size()); // LOG
            } catch (Exception e) {
                log.warn("Failed to deserialize stored control flow graph, falling back to parsing", e);
            }
        }

        if (graph == null && processVersion.getSourceUri() != null) {
            String content = artifactStorageService.downloadContent(processVersion.getSourceUri());
            log.info("Downloaded artifact content. Size: {} bytes", content.length()); // LOG

            if ("BPMN".equalsIgnoreCase(processVersion.getSourceType())) {
                try {
                    graph = bpmnGraphParser.parse(content);
                    log.info("Parsed BPMN. Nodes found: {}", graph.getNodes().size()); // LOG

                    if (graph.getNodes().isEmpty()) {
                        throw new IllegalStateException("BPMN parsed successfully but contains no nodes.");
                    }
                } catch (Exception e) {
                    log.error("BPMN Parsing failed", e); // LOG
                    throw new ImportException("Critical Error: Failed to parse BPMN file.", e);
                }
            } else if ("PLANTUML".equalsIgnoreCase(processVersion.getSourceType())) {
                try {
                    PumlDocument doc = plantUmlParser.parse(content);
                    graph = plantUmlGraphConverter.convert(doc);
                    log.info("Parsed PlantUML. Nodes found: {}", graph.getNodes().size()); // LOG

                    if (graph.getNodes().isEmpty()) {
                        throw new IllegalStateException("PlantUML parsed successfully but contains no nodes.");
                    }
                } catch (Exception e) {
                    log.error("PlantUML Parsing failed", e); // LOG
                    throw new ImportException("Critical Error: Failed to parse PlantUML file.", e);
                }
            }
        }

        // 2. Generate Scenarios from Graph
        if (graph != null) {
            // Debug log graph structure
            log.info("DEBUG: Graph parsed. Total Nodes: {}, Total Edges: {}",
                    graph.getNodes().size(),
                    graph.getEdges().size());

            graph.getNodes().forEach(
                    n -> log.debug("DEBUG: Node ID: {}, Name: {}, Type: {}", n.getId(), n.getName(), n.getType()));

            int maxLoops = request.getMaxLoopIterations() != null ? request.getMaxLoopIterations() : 2;
            log.info("Generating paths with maxLoops: {}", maxLoops); // LOG

            List<List<PathElement>> paths = generatePaths(graph, maxLoops);
            log.info(">>> Path generation finished. Paths found: {}", paths.size()); // LOG

            if (paths.isEmpty()) {
                log.warn("!!! NO PATHS FOUND. Check if Start Node exists and is connected to End Node.");
            }

            int count = 1;
            for (List<PathElement> path : paths) {
                String name = "Scenario " + count;
                String keySuffix = "path-" + count;
                log.info("Creating scenario #{} with {} steps", count, path.size()); // LOG
                createScenarioFromPath(suite, path, name, keySuffix, request.getSpecBindings());
                count++;
            }
        } else {
            log.error("Graph is NULL after parsing attempts!"); // LOG
        }
    }

    private List<List<PathElement>> generatePaths(ControlFlowGraph graph, int maxLoops) {
        GraphNode startNode = graph.getNodes().stream()
                .filter(n -> n.getType() == NodeType.START)
                .findFirst()
                .orElse(null);

        if (startNode == null) {
            log.error("!!! START NODE NOT FOUND in graph. Available nodes: {}",
                    graph.getNodes().stream().map(GraphNode::getType).toList()); // LOG
            return Collections.emptyList();
        }

        log.info("Found START node: {}", startNode.getId()); // LOG

        return explore(startNode, null, graph, new HashMap<>(), false, maxLoops);
    }

    private List<List<PathElement>> explore(GraphNode current, GraphNode stopNode, ControlFlowGraph graph,
            Map<String, Integer> visitCounts, boolean isParallelContext, int maxLoops) {

        if (current == null) {
            log.warn("DEBUG: Explore called with NULL node! Returning empty path.");
            return List.of(new ArrayList<>());
        }
        log.info("DEBUG: Exploring node: ID={} Name='{}' Type={}",
                current.getId(),
                current.getName(),
                current.getType());

        if (stopNode != null && current.getId().equals(stopNode.getId()))
            return List.of(new ArrayList<>());

        int currentCount = visitCounts.getOrDefault(current.getId(), 0);
        if (currentCount >= 1 + maxLoops)
            return List.of(new ArrayList<>()); // Loop limit reached

        Map<String, Integer> newVisitCounts = new HashMap<>(visitCounts);
        newVisitCounts.put(current.getId(), currentCount + 1);

        List<List<PathElement>> results = new ArrayList<>();
        List<GraphEdge> outgoing = graph.getOutgoingEdges(current.getId());

        if (outgoing.isEmpty() && current.getType() != NodeType.END) {
            log.warn("DEBUG: Dead end reached at node {} (Not an END node). No outgoing edges.", current.getId());
        }

        if (current.getType() == NodeType.EXCLUSIVE_GATEWAY) {
            // Branching: Generate separate scenarios for each path
            for (GraphEdge edge : outgoing) {
                GraphNode target = graph.getNode(edge.getTargetId());
                List<List<PathElement>> suffixes = explore(target, stopNode, graph, newVisitCounts, isParallelContext,
                        maxLoops);
                results.addAll(prependEdgeCondition(edge, suffixes));
            }
        } else if (current.getType() == NodeType.INCLUSIVE_GATEWAY) {
            // 1. Singles (Isolation testing) - treat like Exclusive
            for (GraphEdge edge : outgoing) {
                GraphNode target = graph.getNode(edge.getTargetId());
                results.addAll(explore(target, stopNode, graph, newVisitCounts, isParallelContext, maxLoops));
            }

            // 2. Pairwise Combinations (Integration testing)
            // Generate scenarios where pairs of branches run in parallel
            if (outgoing.size() > 1) {
                List<List<GraphEdge>> pairs = generatePairs(outgoing);
                for (List<GraphEdge> pair : pairs) {
                    results.addAll(exploreParallel(current, pair, stopNode, graph, newVisitCounts, maxLoops));
                }
            }

            // 3. All Together (Max Load)
            if (outgoing.size() > 2) {
                results.addAll(exploreParallel(current, outgoing, stopNode, graph, newVisitCounts, maxLoops));
            }
        } else if (current.getType() == NodeType.PARALLEL_GATEWAY && outgoing.size() > 1) {
            // Fork: Traverse all branches and synchronize
            results.addAll(exploreParallel(current, outgoing, stopNode, graph, newVisitCounts, maxLoops));
        } else {
            // Linear node (Start, Action, End, or Join Gateway passing through)
            List<List<PathElement>> suffixes = new ArrayList<>();
            if (outgoing.isEmpty()) {
                suffixes.add(new ArrayList<>());
            } else {
                // Deterministic traversal for linear segments
                GraphEdge nextEdge = outgoing.stream().min(Comparator.comparing(GraphEdge::getId))
                        .orElse(outgoing.get(0));
                suffixes = explore(graph.getNode(nextEdge.getTargetId()), stopNode, graph, newVisitCounts,
                        isParallelContext, maxLoops);
            }

            for (List<PathElement> suffix : suffixes) {
                List<PathElement> path = new ArrayList<>();
                if (current.getType() == NodeType.ACTION) {
                    path.add(new NodeElement(current, isParallelContext));
                }
                // Add condition from the edge we took to get here? No, edges are outgoing.
                // The condition is on the edge LEAVING this node.
                // But we selected 'nextEdge' above.
                if (!outgoing.isEmpty()) {
                    GraphEdge nextEdge = outgoing.stream().min(Comparator.comparing(GraphEdge::getId))
                            .orElse(outgoing.get(0));
                    if (nextEdge.getConditionExpression() != null) {
                        path.add(new ConditionElement(nextEdge.getConditionExpression()));
                    }
                }
                path.addAll(suffix);
                results.add(path);
            }
        }

        // TASK-2024-091: Loop detection is implicit via visitCounts.
        // If we are in a loop, we might have added paths that loop 0, 1, ... maxLoops
        // times.
        // We can tag the path with loop info if needed, but for now the path structure
        // itself
        // contains the repeated nodes.

        return results.isEmpty() ? List.of(new ArrayList<>()) : results;
    }

    private List<List<PathElement>> exploreParallel(GraphNode forkNode, List<GraphEdge> activeEdges, GraphNode stopNode,
            ControlFlowGraph graph, Map<String, Integer> visitCounts, int maxLoops) {
        List<List<PathElement>> results = new ArrayList<>();

        // Find join node relevant to these specific edges
        GraphNode joinNode = findJoinNode(forkNode, activeEdges, graph);

        List<List<List<PathElement>>> branchesVariations = new ArrayList<>();
        for (GraphEdge edge : activeEdges) {
            GraphNode target = graph.getNode(edge.getTargetId());
            branchesVariations.add(explore(target, joinNode, graph, visitCounts, true, maxLoops));
        }

        // Combine one path from each branch sequentially (Simplified Parallel
        // Simulation)
        List<PathElement> combinedPath = new ArrayList<>();
        List<String> trackedAliases = new ArrayList<>();

        for (List<List<PathElement>> branchVars : branchesVariations) {
            if (!branchVars.isEmpty()) {
                List<PathElement> branchPath = branchVars.get(0); // Take first variation for now
                combinedPath.addAll(branchPath);
                // Find last action step alias to track in barrier
                for (int i = branchPath.size() - 1; i >= 0; i--) {
                    if (branchPath.get(i) instanceof NodeElement ne && ne.node.getType() == NodeType.ACTION) {
                        trackedAliases.add(ne.node.getId());
                        break;
                    }
                }
            }
        }

        if (!trackedAliases.isEmpty()) {
            combinedPath.add(new BarrierElement(trackedAliases));
        }

        // Continue from Join
        List<List<PathElement>> suffixes = explore(joinNode, stopNode, graph, visitCounts, false, maxLoops);
        for (List<PathElement> suffix : suffixes) {
            List<PathElement> fullPath = new ArrayList<>(combinedPath);
            fullPath.addAll(suffix);
            results.add(fullPath);
        }

        return results;
    }

    private GraphNode findJoinNode(GraphNode fork, List<GraphEdge> outgoing, ControlFlowGraph graph) {
        if (outgoing.size() <= 1)
            return null;

        // Find reachable nodes for each branch
        List<Set<String>> reachableSets = new ArrayList<>();
        for (GraphEdge edge : outgoing) {
            reachableSets.add(findAllReachable(edge.getTargetId(), graph));
        }

        // Find intersection of all reachable sets (nodes reachable from ALL branches)
        Set<String> intersection = new HashSet<>(reachableSets.get(0));
        for (int i = 1; i < reachableSets.size(); i++) {
            intersection.retainAll(reachableSets.get(i));
        }

        if (intersection.isEmpty())
            return null;

        // Find the topologically first node in the intersection (closest to the fork)
        return findFirstInIntersection(fork.getId(), intersection, graph);
    }

    private Set<String> findAllReachable(String startId, ControlFlowGraph graph) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(startId);
        visited.add(startId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (GraphEdge edge : graph.getOutgoingEdges(current)) {
                if (visited.add(edge.getTargetId())) {
                    queue.add(edge.getTargetId());
                }
            }
        }
        return visited;
    }

    private GraphNode findFirstInIntersection(String startId, Set<String> intersection, ControlFlowGraph graph) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            // If we hit a node in the intersection (and it's not the fork itself), it's the
            // join
            if (!currentId.equals(startId) && intersection.contains(currentId)) {
                return graph.getNode(currentId);
            }

            for (GraphEdge edge : graph.getOutgoingEdges(currentId)) {
                if (visited.add(edge.getTargetId())) {
                    queue.add(edge.getTargetId());
                }
            }
        }
        return null;
    }

    private List<List<GraphEdge>> generatePairs(List<GraphEdge> edges) {
        List<List<GraphEdge>> pairs = new ArrayList<>();
        for (int i = 0; i < edges.size(); i++) {
            for (int j = i + 1; j < edges.size(); j++) {
                pairs.add(List.of(edges.get(i), edges.get(j)));
            }
        }
        return pairs;
    }

    private List<List<PathElement>> prependEdgeCondition(GraphEdge edge, List<List<PathElement>> suffixes) {
        if (edge.getConditionExpression() == null) {
            return suffixes;
        }
        List<List<PathElement>> result = new ArrayList<>();
        for (List<PathElement> suffix : suffixes) {
            List<PathElement> newPath = new ArrayList<>();
            newPath.add(new ConditionElement(edge.getConditionExpression()));
            newPath.addAll(suffix);
            result.add(newPath);
        }
        return result;
    }

    private interface PathElement {
    }

    private record NodeElement(GraphNode node, boolean isAsync) implements PathElement {
    }

    private record BarrierElement(List<String> trackedAliases) implements PathElement {
    }

    private record ConditionElement(String expression) implements PathElement {
    }

    private void createScenarioFromPath(ScenarioSuite suite, List<PathElement> path, String name, String keySuffix,
            Map<String, String> specBindings) {
        log.info("DEBUG: Converting path to scenario. Path length: {} elements", path.size());

        if (path.isEmpty())
            return;

        TestScenario scenario = new TestScenario();
        scenario.setId(UUID.randomUUID());
        scenario.setTenant(suite.getTenant());
        scenario.setSuite(suite);
        scenario.setName(name);
        scenario.setKey(suite.getProcess().getKey() + "-" + keySuffix);
        scenario.setVersion(1);
        scenario.setStatus("DRAFT");
        scenario.setActive(true);
        scenario.setTags(new ArrayList<>(List.of("generated")));
        scenario.setSteps(new ArrayList<>());

        List<AiDataTransferRequest.AvailableOutput> contextOutputs = new ArrayList<>();
        List<String> pathPredicates = new ArrayList<>();
        int score = 0;

        int order = 1;
        for (PathElement element : path) {
            ScenarioStep step = new ScenarioStep();
            step.setId(UUID.randomUUID());
            step.setScenario(scenario);
            step.setOrderIndex(order++);

            if (element instanceof NodeElement ne) {
                GraphNode node = ne.node;
                // TASK-2024-092: Enhanced Scoring Heuristics
                if (node.getType() == NodeType.EXCLUSIVE_GATEWAY || node.getType() == NodeType.INCLUSIVE_GATEWAY) {
                    score += 3; // Branching adds complexity/risk
                } else {
                    score += 1; // Base score for standard node
                }
                step.setAlias(node.getId());
                step.setName(node.getName() != null ? node.getName() : node.getId());
                step.setKind("ACTION");
                step.setChannelType("HTTP_REST"); // Default

                // Try to resolve endpoint using Smart Mapping
                String preferredSpecId = null;
                if (specBindings != null && node.getMetadata() != null) {
                    String target = (String) node.getMetadata().get("target");
                    String lane = (String) node.getMetadata().get("lane");
                    if (target != null && specBindings.containsKey(target)) {
                        preferredSpecId = specBindings.get(target);
                    } else if (lane != null && specBindings.containsKey(lane)) {
                        preferredSpecId = specBindings.get(lane);
                    } else if (node.getName() != null && specBindings.containsKey(node.getName())) {
                        preferredSpecId = specBindings.get(node.getName());
                    }
                }
                EndpointMatcher.MatchedEndpoint matched = endpointMatcher.match(node, suite.getTenant().getId(),
                        preferredSpecId);

                Map<String, Object> action = new HashMap<>();
                action.put("mode", ne.isAsync ? "FIRE_AND_FORGET" : "SYNC");
                Map<String, Object> input = new HashMap<>();

                if (matched != null) {
                    input.put("method", matched.method());
                    input.put("url", matched.urlTemplate());
                    Map<String, Object> endpointRef = new HashMap<>();
                    endpointRef.put("protocolId", matched.protocolId());
                    endpointRef.put("serviceName", matched.serviceName());
                    endpointRef.put("endpointName", matched.endpointName());
                    step.setEndpointRef(endpointRef);

                    // AI Data Transfer Suggestion
                    if (!contextOutputs.isEmpty()) {
                        try {
                            AiDataTransferRequest transferReq = new AiDataTransferRequest();
                            transferReq.setTargetStepName(node.getName());
                            transferReq.setTargetSchema(matched.endpointName()); // Using endpoint name as proxy for
                                                                                 // schema for now
                            transferReq.setAvailableOutputs(new ArrayList<>(contextOutputs));

                            AiDataTransferResponse transferResp = aiService.suggestDataTransfer(transferReq);
                            if (transferResp != null && transferResp.getMapping() != null) {
                                input.put("body", transferResp.getMapping());
                            }
                        } catch (Exception e) {
                            log.warn("Failed to suggest data transfer for step {}", step.getAlias(), e);
                        }
                    }
                } else {
                    input.put("method", "POST");
                    input.put("url", "http://placeholder-service/api/" + node.getId());
                }
                action.put("inputTemplate", input);
                action.put("meta", Map.of("timeoutMs", 5000));
                step.setAction(action);

                Map<String, Object> expectations = new HashMap<>();
                expectations.put("expectedStatusCode", 200);
                step.setExpectations(expectations);

                // Add to context for next steps
                contextOutputs.add(new AiDataTransferRequest.AvailableOutput(step.getAlias(), step.getName(),
                        "Output of " + step.getName()));
            } else if (element instanceof BarrierElement be) {
                score += 5; // Parallelism is complex
                step.setAlias("barrier_" + UUID.randomUUID().toString().substring(0, 8));
                step.setName("Wait for Parallel Branches");
                step.setKind("BARRIER");
                step.setChannelType("CONTROL"); // Control channel for barrier

                Map<String, Object> action = new HashMap<>();
                Map<String, Object> meta = new HashMap<>();
                meta.put("trackedSteps", be.trackedAliases);
                action.put("meta", meta);
                step.setAction(action);
            } else if (element instanceof ConditionElement ce) {
                // TASK-2024-089: Collect predicates
                pathPredicates.add(ce.expression);
                score += 2; // Conditions add complexity
                continue; // Don't create a step for condition
            }

            scenario.getSteps().add(step);
        }

        // TASK-2024-092: Set Score
        scenario.setScore(score);

        // TASK-2024-089: Store predicates in metadata
        if (!pathPredicates.isEmpty()) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("pathPredicates", pathPredicates);
            scenario.setMetadata(meta);
        }

        testScenarioRepository.save(scenario);

        // TASK-2024-090: Generate Data Criteria (EP/BVA)
        if (!pathPredicates.isEmpty()) {
            generateBvaDataSets(scenario, pathPredicates);
        }

        log.info("Generated scenario {} with {} steps", scenario.getKey(), scenario.getSteps().size());
    }

    private void generateBvaDataSets(TestScenario scenario, List<String> predicates) {
        // TASK-2024-090: Generate TestDataSet with criteria based on predicates
        // For MVP, we create a placeholder dataset with instructions for the Data
        // Resolver

        TestDataSet dataSet = new TestDataSet();
        dataSet.setId(UUID.randomUUID());
        dataSet.setTenant(scenario.getTenant());
        dataSet.setScenario(scenario);
        dataSet.setScope("SCENARIO");
        dataSet.setName("BVA Criteria for " + scenario.getName());
        dataSet.setOrigin("AI_GENERATED");
        dataSet.setStatus("READY");
        dataSet.setTags(List.of("bva", "generated"));

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("_description", "Boundary Value Analysis criteria derived from path predicates");
        criteria.put("_predicates", predicates);

        // TASK-2024-090: Robust parsing for EP/BVA
        // Matches: variable operator value (e.g., "amount >= 1000", "status ==
        // 'ACTIVE'")
        Pattern pattern = Pattern.compile("(\\w+)\\s*(>=|<=|==|>|<)\\s*['\"]?(.*?)['\"]?$");

        for (String pred : predicates) {
            Matcher m = pattern.matcher(pred.trim());
            if (m.find()) {
                String key = m.group(1);
                String op = m.group(2);
                String val = m.group(3);

                Map<String, Object> spec = new HashMap<>();
                spec.put("originalPredicate", pred);

                if (op.equals("==")) {
                    // Exact match (Equivalence Partition)
                    criteria.put(key, val);
                } else {
                    // Boundary Analysis hint for Data Resolver
                    spec.put("semanticCriteria", "Value satisfying " + pred + " (Boundary Analysis)");
                    spec.put("boundaryOp", op);
                    spec.put("boundaryVal", val);
                    criteria.put(key, spec);
                }
            }
        }

        dataSet.setData(criteria);
        testDataSetRepository.save(dataSet);
    }

    private boolean hydrateSuiteData(ScenarioSuite suite, UUID environmentId) {
        try {
            // 1. Generate Data using AI Service (Synchronous call within Async job)
            AiGenerateSuiteResponse response = aiService.generateDataForSuite(suite.getId(), environmentId);

            if (response == null || response.scenarioData() == null) {
                log.warn("Hydration returned empty response for suite {}", suite.getId());
                return false;
            }

            // 2. Prepare Data Set content
            Map<String, Object> dataSetContent = new HashMap<>();
            if (response.suiteContext() != null) {
                dataSetContent.put("global", response.suiteContext());
            }

            List<TestScenario> scenarios = testScenarioRepository.findBySuiteIdWithSuite(suite.getId());
            boolean hasUpdates = false;

            for (TestScenario scenario : scenarios) {
                AiGenerateScenarioResponse scenarioData = response.scenarioData().get(scenario.getId());
                if (scenarioData != null && scenarioData.getStepData() != null) {
                    for (Map.Entry<String, Object> entry : scenarioData.getStepData().entrySet()) {
                        String stepAlias = entry.getKey();
                        Object data = entry.getValue();

                        // Key format: scenarioKey_stepAlias to ensure uniqueness in suite scope
                        String dataKey = scenario.getKey() + "_" + stepAlias;
                        dataSetContent.put(dataKey, data);

                        // 3. Link Step to Data
                        scenario.getSteps().stream()
                                .filter(s -> s.getAlias().equals(stepAlias))
                                .findFirst()
                                .ifPresent(step -> {
                                    if (step.getAction() != null && step.getAction().containsKey("inputTemplate")) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> input = (Map<String, Object>) step.getAction()
                                                .get("inputTemplate");
                                        // Only override if body is not already set by DataTransferAgent
                                        if (!input.containsKey("body") || input.get("body") == null) {
                                            input.put("body", "{{data." + dataKey + "}}");
                                        }
                                    }
                                });
                    }
                    hasUpdates = true;
                }
            }

            if (hasUpdates) {
                testScenarioRepository.saveAll(scenarios);

                // 4. Create and Save TestDataSet
                createHydratedDataSet(suite, dataSetContent);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to hydrate suite data", e);
            // Don't fail the whole generation, just log error
            return false;
        }
    }

    private void createHydratedDataSet(ScenarioSuite suite, Map<String, Object> content) {
        TestDataSet dataSet = new TestDataSet();
        dataSet.setId(UUID.randomUUID());
        dataSet.setTenant(suite.getTenant());
        dataSet.setSuite(suite);
        dataSet.setScope("SUITE");
        dataSet.setName("AI Generated Data for " + suite.getName());
        dataSet.setOrigin("AI_GENERATED");
        dataSet.setStatus("READY");
        dataSet.setTags(List.of("hydrated", "ai"));
        dataSet.setData(content);
        testDataSetRepository.save(dataSet);
    }
}
