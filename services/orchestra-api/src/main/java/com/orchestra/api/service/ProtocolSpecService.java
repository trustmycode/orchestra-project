package com.orchestra.api.service;

import com.orchestra.domain.dto.ProtocolSpecSummary;
import com.orchestra.domain.mapper.ProtocolSpecMapper;
import com.orchestra.domain.repository.ProtocolSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProtocolSpecService {

    private final ProtocolSpecRepository protocolSpecRepository;
    private final ProtocolSpecMapper protocolSpecMapper;

    @Transactional(readOnly = true)
    public List<ProtocolSpecSummary> findAll() {
        return protocolSpecRepository.findAll().stream()
                .map(protocolSpecMapper::toDto)
                .collect(Collectors.toList());
    }
}
