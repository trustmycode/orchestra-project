package com.orchestra.api.controller;

import com.orchestra.api.service.DataResolverService;
import com.orchestra.domain.dto.DataResolverDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/data-resolvers")
@RequiredArgsConstructor
public class DataResolverController {

    private final DataResolverService dataResolverService;

    @GetMapping
    public ResponseEntity<List<DataResolverDto>> getAll() {
        return ResponseEntity.ok(dataResolverService.findAll());
    }

    @PostMapping
    public ResponseEntity<DataResolverDto> create(@RequestBody DataResolverDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dataResolverService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DataResolverDto> update(@PathVariable UUID id, @RequestBody DataResolverDto dto) {
        return ResponseEntity.ok(dataResolverService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        dataResolverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

