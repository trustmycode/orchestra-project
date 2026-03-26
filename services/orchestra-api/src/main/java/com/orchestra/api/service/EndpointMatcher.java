package com.orchestra.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.domain.dto.AiMappingRequest;
import com.orchestra.domain.dto.AiMappingResponse;
import com.orchestra.domain.model.ProtocolSpec;
import com.orchestra.domain.model.graph.GraphNode;
import com.orchestra.domain.repository.ProtocolSpecRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndpointMatcher {

    private final ProtocolSpecRepository protocolSpecRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    
    private static final Set<String> STOP_WORDS = Set.of("a", "an", "the", "in", "on", "at", "to", "for", "of", "with");

    public record MatchedEndpoint(
            String protocolId,
            String serviceName,
            String endpointName, // usually operationId or path
            String method,
            String urlTemplate
    ) {}

    public MatchedEndpoint match(GraphNode node, UUID tenantId) {
        return match(node, tenantId, null);
    }

    public MatchedEndpoint match(GraphNode node, UUID tenantId, String preferredSpecId) {
        List<ProtocolSpec> specs = protocolSpecRepository.findAllByTenantId(tenantId);

        if (specs.isEmpty()) {
            return null;
        }

        List<Candidate> candidates = new ArrayList<>();
        
        // 1. Flatten candidates
        for (ProtocolSpec spec : specs) {
            if (preferredSpecId != null && !spec.getId().toString().equals(preferredSpecId)) {
                continue;
            }

            if (spec.getParsedSummary() != null && spec.getParsedSummary().containsKey("endpoints")) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> endpoints = (List<Map<String, String>>) spec.getParsedSummary().get("endpoints");
                for (Map<String, String> ep : endpoints) {
                    candidates.add(new Candidate(spec, ep));
                }
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        // 2. Strategy 1: Exact Match (Operation ID)
        // Assuming BPMN metadata might contain 'operationId'
        if (node.getMetadata() != null && node.getMetadata().containsKey("operationId")) {
            String targetOpId = (String) node.getMetadata().get("operationId");
            for (Candidate c : candidates) {
                if (targetOpId.equalsIgnoreCase(c.operationId)) {
                    log.info("Exact match found for node {}: {}", node.getName(), c.operationId);
                    return toMatched(c);
                }
            }
        }

        // 3. Strategy 2: Deterministic Fuzzy Match
        for (Candidate c : candidates) {
            if (isFuzzyMatch(node.getName(), c)) {
                log.info("Fuzzy match found for node {}: {} {}", node.getName(), c.method, c.path);
                return toMatched(c);
            }
        }

        // 4. Strategy 3: AI Fallback
        AiMappingRequest request = new AiMappingRequest();
        request.setTaskName(node.getName());
        request.setTenantId(tenantId);
        if (node.getMetadata() != null) {
            request.setTaskDescription((String) node.getMetadata().get("description"));
        }
        
        // Populate candidates for AI
        List<AiMappingRequest.CandidateDto> candidateDtos = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            Candidate c = candidates.get(i);
            candidateDtos.add(new AiMappingRequest.CandidateDto(
                i, c.method, c.path, c.summary, c.operationId, c.description
            ));
        }
        request.setCandidates(candidateDtos);

        try {
            AiMappingResponse response = aiService.mapEndpoint(request);
            if (response != null && response.getSelectedCandidateIndex() != null && response.getSelectedCandidateIndex() >= 0) {
                if (response.getSelectedCandidateIndex() < candidates.size()) {
                    Candidate selected = candidates.get(response.getSelectedCandidateIndex());
                    log.info("AI matched node '{}' to endpoint '{}' (Confidence: {})", node.getName(), selected.path, response.getConfidence());
                    return toMatched(selected);
                }
            }
        } catch (Exception e) {
            log.warn("AI mapping failed for node {}", node.getName(), e);
        }

        return null;
    }

    private MatchedEndpoint toMatched(Candidate c) {
        // Construct URL template. Assuming base URL is handled by environment config, here we just put path.
        // Convert OpenAPI path params {id} to Orchestra placeholders {{id}}
        String adjustedPath = c.path.replaceAll("\\{([^}]+)\\}", "{{$1}}");
        String url = "http://" + c.spec.getServiceName() + adjustedPath;
        return new MatchedEndpoint(c.spec.getProtocolId(), c.spec.getServiceName(), c.operationId != null ? c.operationId : c.path, c.method, url);
    }

    private boolean isFuzzyMatch(String taskName, Candidate candidate) {
        if (taskName == null) return false;
        
        Set<String> taskTokens = tokenize(taskName);
        if (taskTokens.isEmpty()) return false;

        // Check against OperationID
        if (candidate.operationId != null) {
            Set<String> opTokens = tokenize(candidate.operationId);
            if (opTokens.containsAll(taskTokens)) return true;
        }

        // Check against Summary + Path
        Set<String> candidateTokens = tokenize(candidate.path);
        if (candidate.summary != null) {
            candidateTokens.addAll(tokenize(candidate.summary));
        }

        // Require significant overlap (e.g. all task tokens present in candidate)
        // This is a strict fuzzy match to avoid false positives
        return candidateTokens.containsAll(taskTokens);
    }

    private Set<String> tokenize(String input) {
        return Stream.of(input.split("[^a-zA-Z0-9]"))
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .filter(s -> !STOP_WORDS.contains(s))
                .collect(Collectors.toSet());
    }

    private record Candidate(ProtocolSpec spec, String method, String path, String operationId, String summary, String description) {
        public Candidate(ProtocolSpec spec, Map<String, String> ep) {
            this(spec, ep.get("method"), ep.get("path"), ep.get("operationId"), ep.get("summary"), ep.get("description"));
        }
    }
}

