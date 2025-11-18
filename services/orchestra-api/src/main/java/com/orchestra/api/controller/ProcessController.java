package com.orchestra.api.controller;

import com.orchestra.api.dto.ProcessModel;
import com.orchestra.api.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/processes")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;

    @GetMapping
    public ResponseEntity<List<ProcessModel>> getProcesses() {
        return ResponseEntity.ok(processService.findAll());
    }
}
