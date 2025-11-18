package com.orchestra.api.controller;

import com.orchestra.api.dto.TestScenarioDetail;
import com.orchestra.api.dto.TestScenarioSummary;
import com.orchestra.api.service.TestScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scenarios")
@RequiredArgsConstructor
public class TestScenarioController {

    private final TestScenarioService testScenarioService;

    @GetMapping
    public ResponseEntity<List<TestScenarioSummary>> getScenarios() {
        return ResponseEntity.ok(testScenarioService.findAll());
    }

    @PostMapping
    public ResponseEntity<TestScenarioDetail> createScenario(@RequestBody TestScenarioDetail createRequest) {
        TestScenarioDetail created = testScenarioService.create(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestScenarioDetail> getScenarioById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(testScenarioService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestScenarioDetail> updateScenario(@PathVariable("id") UUID id, @RequestBody TestScenarioDetail updateRequest) {
        return ResponseEntity.ok(testScenarioService.update(id, updateRequest));
    }
}
