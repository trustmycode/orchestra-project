package com.orchestra.api.controller;

import com.orchestra.domain.dto.TestDataSetDetail;
import com.orchestra.api.service.TestDataSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/test-data-sets")
@RequiredArgsConstructor
public class TestDataSetController {

    private final TestDataSetService testDataSetService;

    @GetMapping
    public ResponseEntity<List<TestDataSetDetail>> getAllDataSets() {
        return ResponseEntity.ok(testDataSetService.findAll());
    }

    @PostMapping
    public ResponseEntity<TestDataSetDetail> createDataSet(@RequestBody TestDataSetDetail createRequest) {
        TestDataSetDetail created = testDataSetService.create(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestDataSetDetail> getDataSetById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(testDataSetService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestDataSetDetail> updateDataSet(
            @PathVariable("id") UUID id,
            @RequestBody TestDataSetDetail updateRequest
    ) {
        return ResponseEntity.ok(testDataSetService.update(id, updateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDataSet(@PathVariable("id") UUID id) {
        testDataSetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
