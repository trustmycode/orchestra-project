package com.orchestra.api.controller;

import com.orchestra.domain.dto.DbConnectionProfileDto;
import com.orchestra.domain.dto.EnvironmentDto;
import com.orchestra.domain.dto.KafkaClusterProfileDto;
import com.orchestra.domain.mapper.EnvironmentMapper;
import com.orchestra.api.service.EnvironmentService;
import com.orchestra.domain.model.DbConnectionProfile;
import com.orchestra.domain.model.Environment;
import com.orchestra.domain.model.KafkaClusterProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/environments")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;
    private final EnvironmentMapper mapper;

    @GetMapping
    public ResponseEntity<List<EnvironmentDto>> getEnvironments() {
        return ResponseEntity.ok(environmentService.findAllEnvironments().stream()
                .map(mapper::toDto)
                .toList());
    }

    @PostMapping
    public ResponseEntity<EnvironmentDto> createEnvironment(@RequestBody EnvironmentDto dto) {
        Environment saved = environmentService.createEnvironment(mapper.toEntity(dto));
        return ResponseEntity.ok(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnvironmentDto> updateEnvironment(@PathVariable UUID id, @RequestBody EnvironmentDto dto) {
        Environment updated = environmentService.updateEnvironment(id, mapper.toEntity(dto));
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnvironment(@PathVariable UUID id) {
        environmentService.deleteEnvironment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profiles/db")
    public ResponseEntity<List<DbConnectionProfileDto>> getDbProfiles() {
        return ResponseEntity.ok(environmentService.findAllDbProfiles().stream()
                .map(mapper::toDto)
                .toList());
    }

    @PostMapping("/profiles/db")
    public ResponseEntity<DbConnectionProfileDto> createDbProfile(@RequestBody DbConnectionProfileDto dto) {
        DbConnectionProfile saved = environmentService.createDbProfile(mapper.toEntity(dto));
        return ResponseEntity.ok(mapper.toDto(saved));
    }

    @DeleteMapping("/profiles/db/{id}")
    public ResponseEntity<Void> deleteDbProfile(@PathVariable UUID id) {
        environmentService.deleteDbProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profiles/kafka")
    public ResponseEntity<List<KafkaClusterProfileDto>> getKafkaProfiles() {
        return ResponseEntity.ok(environmentService.findAllKafkaProfiles().stream()
                .map(mapper::toDto)
                .toList());
    }

    @PostMapping("/profiles/kafka")
    public ResponseEntity<KafkaClusterProfileDto> createKafkaProfile(@RequestBody KafkaClusterProfileDto dto) {
        KafkaClusterProfile saved = environmentService.createKafkaProfile(mapper.toEntity(dto));
        return ResponseEntity.ok(mapper.toDto(saved));
    }

    @DeleteMapping("/profiles/kafka/{id}")
    public ResponseEntity<Void> deleteKafkaProfile(@PathVariable UUID id) {
        environmentService.deleteKafkaProfile(id);
        return ResponseEntity.noContent().build();
    }
}
