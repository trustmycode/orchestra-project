package com.orchestra.api.controller;

import com.orchestra.domain.dto.TestRunCreateRequest;
import com.orchestra.domain.dto.TestRunDetail;
import com.orchestra.api.service.TestRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/testruns")
@RequiredArgsConstructor
public class TestRunController {

    private final TestRunService testRunService;

    @PostMapping
    public ResponseEntity<TestRunDetail> runTest(@RequestBody TestRunCreateRequest request) {
        TestRunDetail result = testRunService.createAndRunTest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestRunDetail> getTestRun(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(testRunService.findRunById(id));
    }
}
