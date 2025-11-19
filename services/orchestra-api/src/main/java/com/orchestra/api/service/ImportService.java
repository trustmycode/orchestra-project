package com.orchestra.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.orchestra.api.exception.ImportException;
import com.orchestra.domain.model.Process;
import com.orchestra.domain.model.ProcessVersion;
import com.orchestra.domain.model.ProtocolSpec;
import com.orchestra.domain.model.Tenant;
import com.orchestra.domain.repository.ProcessRepository;
import com.orchestra.domain.repository.ProcessVersionRepository;
import com.orchestra.domain.repository.ProtocolSpecRepository;
import com.orchestra.domain.repository.TenantRepository;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ProcessRepository processRepository;
    private final ProcessVersionRepository processVersionRepository;
    private final ProtocolSpecRepository protocolSpecRepository;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;
    private final ArtifactStorageService artifactStorageService;
    private final OpenAPIV3Parser openApiParser = new OpenAPIV3Parser();

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Transactional
    public ProcessVersion importBpmn(MultipartFile file) {
        BpmnModelInstance modelInstance = parseBpmn(file);
        Collection<org.camunda.bpm.model.bpmn.instance.Process> processes =
                modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class);
        if (processes.isEmpty()) {
            throw new ImportException("BPMN file must contain at least one process definition.");
        }

        org.camunda.bpm.model.bpmn.instance.Process bpmnProcess = processes.iterator().next();
        String processKey = bpmnProcess.getId();
        if (processKey == null || processKey.isBlank()) {
            throw new ImportException("Process definition in BPMN must have an 'id'.");
        }

        Tenant tenant = getDefaultTenant();
        Process process = processRepository.findByTenantIdAndKey(DEFAULT_TENANT_ID, processKey)
                .orElseGet(() -> createProcess(processKey, tenant));

        int nextVersion = processVersionRepository.findTopByProcessOrderByVersionDesc(process)
                .map(ProcessVersion::getVersion)
                .map(version -> version + 1)
                .orElse(1);

        ProcessVersion processVersion = new ProcessVersion();
        processVersion.setId(UUID.randomUUID());
        processVersion.setProcess(process);
        processVersion.setVersion(nextVersion);
        processVersion.setName(bpmnProcess.getName());
        processVersion.setSourceType("BPMN");

        String s3Key = "processes/" + processVersion.getId() + "/diagram.bpmn";
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            artifactStorageService.upload(s3Key, content);
            processVersion.setSourceUri(s3Key);
        } catch (IOException e) {
            throw new ImportException("Failed to read BPMN file for upload.", e);
        }

        return processVersionRepository.save(processVersion);
    }

    @Transactional
    public ProtocolSpec importOpenApi(String protocolId, String serviceName, MultipartFile file) {
        OpenAPI openAPI = parseOpenApi(file);
        if (openAPI.getInfo() == null || openAPI.getInfo().getVersion() == null) {
            throw new ImportException("OpenAPI specification must contain the info.version field.");
        }

        Tenant tenant = getDefaultTenant();
        String version = openAPI.getInfo().getVersion();
        protocolSpecRepository.findByTenantIdAndServiceNameAndVersion(DEFAULT_TENANT_ID, serviceName, version)
                .ifPresent(existing -> {
                    throw new ImportException("Protocol specification already exists for service '%s' and version '%s'."
                            .formatted(serviceName, version));
                });

        ProtocolSpec protocolSpec = new ProtocolSpec();
        protocolSpec.setId(UUID.randomUUID());
        protocolSpec.setTenant(tenant);
        protocolSpec.setProtocolId(protocolId);
        protocolSpec.setServiceName(serviceName);
        protocolSpec.setVersion(version);
        protocolSpec.setParsedSummary(buildSummary(openAPI));

        return protocolSpecRepository.save(protocolSpec);
    }

    private Process createProcess(String processKey, Tenant tenant) {
        Process process = new Process();
        process.setId(UUID.randomUUID());
        process.setTenant(tenant);
        process.setKey(processKey);
        return processRepository.save(process);
    }

    private Tenant getDefaultTenant() {
        return tenantRepository.findById(DEFAULT_TENANT_ID)
                .orElseThrow(() -> new ImportException("Default tenant is not configured."));
    }

    private BpmnModelInstance parseBpmn(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return Bpmn.readModelFromStream(inputStream);
        } catch (IOException e) {
            throw new ImportException("Failed to read BPMN file.", e);
        } catch (Exception e) {
            throw new ImportException("Failed to parse BPMN file.", e);
        }
    }

    private OpenAPI parseOpenApi(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            SwaggerParseResult result = openApiParser.readContents(content);
            if (result == null || result.getOpenAPI() == null) {
                String message = result != null && result.getMessages() != null && !result.getMessages().isEmpty()
                        ? String.join(", ", result.getMessages())
                        : "Unknown parsing error.";
                throw new ImportException("Could not parse OpenAPI specification. " + message);
            }
            return result.getOpenAPI();
        } catch (IOException e) {
            throw new ImportException("Failed to read OpenAPI file.", e);
        }
    }

    private Map<String, Object> buildSummary(OpenAPI openAPI) {
        ObjectNode summaryNode = objectMapper.createObjectNode();
        summaryNode.put("title", openAPI.getInfo() != null ? openAPI.getInfo().getTitle() : null);
        summaryNode.put("pathCount", openAPI.getPaths() != null ? openAPI.getPaths().size() : 0);
        return objectMapper.convertValue(summaryNode, new TypeReference<Map<String, Object>>() {
        });
    }
}
