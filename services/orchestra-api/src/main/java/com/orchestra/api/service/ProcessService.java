package com.orchestra.api.service;

import com.orchestra.domain.dto.ProcessModel;
import com.orchestra.domain.dto.ProcessVisualizationResponse;
import com.orchestra.domain.mapper.ProcessMapper;
import com.orchestra.domain.model.Process;
import com.orchestra.domain.model.ProcessVersion;
import com.orchestra.domain.repository.ProcessRepository;
import com.orchestra.domain.repository.ProcessVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
}
