package com.orchestra.api.controller;

import com.orchestra.domain.model.ProcessVersion;
import com.orchestra.domain.model.ProtocolSpec;
import com.orchestra.api.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping("/processes/import/bpmn")
    public ResponseEntity<Map<String, UUID>> importBpmnProcess(@RequestParam("file") MultipartFile file) {
        ProcessVersion processVersion = importService.importBpmn(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "processId", processVersion.getProcess().getId(),
                        "processVersionId", processVersion.getId()));
    }

    @PostMapping("/specs/import")
    public ResponseEntity<ProtocolSpec> importSpec(@RequestParam("protocolId") String protocolId,
                                                   @RequestParam("serviceName") String serviceName,
                                                   @RequestParam("file") MultipartFile file) {
        ProtocolSpec protocolSpec = importService.importOpenApi(protocolId, serviceName, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(protocolSpec);
    }
}
