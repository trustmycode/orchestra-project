package com.orchestra.api.service;

import com.orchestra.domain.dto.TestDataSetDetail;
import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.domain.mapper.TestDataSetMapper;
import com.orchestra.domain.model.Tenant;
import com.orchestra.domain.model.TestDataSet;
import com.orchestra.domain.repository.TenantRepository;
import com.orchestra.domain.repository.TestDataSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestDataSetService {

    private final TestDataSetRepository testDataSetRepository;
    private final TenantRepository tenantRepository;
    private final TestDataSetMapper mapper;
    private final DataIndexerService dataIndexerService;

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Transactional(readOnly = true)
    public List<TestDataSetDetail> findAll() {
        return testDataSetRepository.findAll().stream()
                .map(mapper::toDetail)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestDataSetDetail findById(UUID id) {
        TestDataSet dataSet = testDataSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDataSet not found with id: " + id));
        return mapper.toDetail(dataSet);
    }

    @Transactional
    public TestDataSetDetail create(TestDataSetDetail dto) {
        TestDataSet dataSet = mapper.toEntity(dto);
        dataSet.setId(UUID.randomUUID());

        Tenant tenant = tenantRepository.findById(DEFAULT_TENANT_ID)
                .orElseThrow(() -> new IllegalStateException("Default tenant not found"));
        dataSet.setTenant(tenant);

        TestDataSet saved = testDataSetRepository.save(dataSet);
        dataIndexerService.indexAsync(saved);
        return mapper.toDetail(saved);
    }

    @Transactional
    public TestDataSetDetail update(UUID id, TestDataSetDetail dto) {
        TestDataSet dataSet = testDataSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDataSet not found with id: " + id));

        mapper.updateEntityFromDto(dataSet, dto);
        TestDataSet updated = testDataSetRepository.save(dataSet);
        dataIndexerService.reindexAsync(updated);
        return mapper.toDetail(updated);
    }

    public void delete(UUID id) {
        if (!testDataSetRepository.existsById(id)) {
            throw new ResourceNotFoundException("TestDataSet not found with id: " + id);
        }
        dataIndexerService.removeAsync(id);
        testDataSetRepository.deleteById(id);
    }
}
