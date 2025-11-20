package com.orchestra.api.service;

import com.orchestra.domain.dto.ProcessModel;
import com.orchestra.domain.dto.ProcessParticipant;
import com.orchestra.domain.dto.ProcessVisualizationResponse;
import com.orchestra.domain.mapper.ProcessMapper;
import com.orchestra.domain.model.Process;
import com.orchestra.domain.model.ProcessVersion;
import com.orchestra.domain.repository.ProcessRepository;
import com.orchestra.domain.repository.ProcessVersionRepository;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ProcessRepository processRepository;
    private final ProcessVersionRepository processVersionRepository;
    private final ProcessMapper processMapper;
    private final ArtifactStorageService artifactStorageService;

    @Transactional(readOnly = true)
    public List<ProcessModel> findAll() {
        return processVersionRepository.findAll().stream()
                .map(processMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProcessVisualizationResponse getVisualization(UUID processId) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found"));
        ProcessVersion version = processVersionRepository.findTopByProcessOrderByVersionDesc(process)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process version not found"));

        if (version.getSourceUri() == null || version.getSourceUri().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No source artifact stored for process");
        }

        String url = artifactStorageService.generatePresignedUrl(version.getSourceUri());
        return ProcessVisualizationResponse.builder()
                .processId(processId.toString())
                .format(version.getSourceType())
                .sourceUrl(url)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProcessParticipant> getParticipants(UUID processId) {
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found"));
        ProcessVersion version = processVersionRepository.findTopByProcessOrderByVersionDesc(process)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process version not found"));

        if (version.getSourceUri() == null || version.getSourceUri().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No source artifact stored for process");
        }

        if (!"BPMN".equalsIgnoreCase(version.getSourceType())) {
            return List.of();
        }

        String content = artifactStorageService.downloadContent(version.getSourceUri());

        try (ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(stream);
            List<ProcessParticipant> result = new ArrayList<>();

            Collection<Participant> participants = modelInstance.getModelElementsByType(Participant.class);
            if (!participants.isEmpty()) {
                participants.forEach(p -> result.add(new ProcessParticipant(p.getId(), p.getName())));
                return result;
            }

            Collection<Lane> lanes = modelInstance.getModelElementsByType(Lane.class);
            if (!lanes.isEmpty()) {
                lanes.forEach(l -> result.add(new ProcessParticipant(l.getId(), l.getName())));
                return result;
            }

            Collection<org.camunda.bpm.model.bpmn.instance.Process> processes = modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);
            processes.forEach(p -> result.add(new ProcessParticipant(p.getId(), p.getName() != null ? p.getName() : "Main Process")));

            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse BPMN", e);
        }
    }
}
