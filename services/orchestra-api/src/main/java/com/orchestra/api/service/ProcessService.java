package com.orchestra.api.service;

import com.orchestra.api.dto.ProcessModel;
import com.orchestra.api.mapper.ProcessMapper;
import com.orchestra.api.repository.ProcessVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ProcessVersionRepository processVersionRepository;
    private final ProcessMapper processMapper;

    @Transactional(readOnly = true)
    public List<ProcessModel> findAll() {
        return processVersionRepository.findAll().stream()
                .map(processMapper::toDto)
                .collect(Collectors.toList());
    }
}
