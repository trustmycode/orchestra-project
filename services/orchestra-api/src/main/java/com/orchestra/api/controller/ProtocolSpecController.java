package com.orchestra.api.controller;

import com.orchestra.domain.dto.ProtocolSpecSummary;
import com.orchestra.api.service.ProtocolSpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/specs")
@RequiredArgsConstructor
public class ProtocolSpecController {

    private final ProtocolSpecService protocolSpecService;

    @GetMapping
    public ResponseEntity<List<ProtocolSpecSummary>> getSpecs() {
        return ResponseEntity.ok(protocolSpecService.findAll());
    }
}
