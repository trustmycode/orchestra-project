package com.orchestra.api.security;

import com.orchestra.api.service.ProcessService;
import com.orchestra.domain.context.TenantContext;
import com.orchestra.domain.dto.ProcessModel;
import com.orchestra.domain.model.Process;
import com.orchestra.domain.model.ProcessVersion;
import com.orchestra.domain.model.Tenant;
import com.orchestra.domain.repository.ProcessRepository;
import com.orchestra.domain.repository.ProcessVersionRepository;
import com.orchestra.domain.repository.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class TenantIsolationIntegrationTest {

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessVersionRepository processVersionRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProcessService processService;

    private Tenant tenantA;
    private Tenant tenantB;

    @BeforeEach
    public void setup() {
        // Create Tenants
        tenantA = new Tenant();
        tenantA.setId(UUID.randomUUID());
        tenantA.setName("Tenant A");
        tenantRepository.save(tenantA);

        tenantB = new Tenant();
        tenantB.setId(UUID.randomUUID());
        tenantB.setName("Tenant B");
        tenantRepository.save(tenantB);

        // Create Data for Tenant A
        createProcessWithVersion("proc-a-1", "Process A1", tenantA);
        createProcessWithVersion("proc-a-2", "Process A2", tenantA);

        // Create Data for Tenant B
        createProcessWithVersion("proc-b-1", "Process B1", tenantB);
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
        processVersionRepository.deleteAll();
        processRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testFindAll_TenantA_ShouldSeeOnlyOwnProcesses() {
        // Simulate request for Tenant A
        TenantContext.setTenantId(tenantA.getId());

        List<ProcessModel> processes = processService.findAll();

        assertThat(processes).hasSize(2);
        assertThat(processes).extracting(ProcessModel::getName)
                .containsExactlyInAnyOrder("Process A1", "Process A2");
    }

    @Test
    @Transactional
    public void testFindAll_TenantB_ShouldSeeOnlyOwnProcesses() {
        // Simulate request for Tenant B
        TenantContext.setTenantId(tenantB.getId());

        List<ProcessModel> processes = processService.findAll();

        assertThat(processes).hasSize(1);
        assertThat(processes.get(0).getName()).isEqualTo("Process B1");
    }

    private void createProcessWithVersion(String key, String name, Tenant tenant) {
        Process process = new Process();
        process.setId(UUID.randomUUID());
        process.setKey(key);
        process.setTenant(tenant);
        processRepository.save(process);

        ProcessVersion version = new ProcessVersion();
        version.setId(UUID.randomUUID());
        version.setProcess(process);
        version.setVersion(1);
        version.setName(name);
        version.setSourceType("BPMN");
        version.setPublished(true);
        processVersionRepository.save(version);
    }
}

