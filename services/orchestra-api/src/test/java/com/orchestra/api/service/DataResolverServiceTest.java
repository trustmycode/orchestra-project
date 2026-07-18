package com.orchestra.api.service;

import com.orchestra.api.security.DatabaseAccessPolicy;
import com.orchestra.domain.mapper.DataResolverMapper;
import com.orchestra.domain.repository.DataResolverRepository;
import com.orchestra.domain.repository.DbConnectionProfileRepository;
import com.orchestra.domain.repository.EnvironmentRepository;
import com.orchestra.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataResolverServiceTest {

    @Mock
    private EnvironmentRepository environmentRepository;
    @Mock
    private DbConnectionProfileRepository dbProfileRepository;
    @Mock
    private DataResolverRepository dataResolverRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private DataResolverMapper dataResolverMapper;
    @Mock
    private VectorStore vectorStore;

    private DataResolverService service;

    @BeforeEach
    void setUp() {
        service = new DataResolverService(
                environmentRepository,
                dbProfileRepository,
                dataResolverRepository,
                tenantRepository,
                dataResolverMapper,
                vectorStore,
                new DatabaseAccessPolicy("localhost"));
    }

    @Test
    void generatesSyntheticDataWhenEnvironmentIsUnavailable() {
        UUID environmentId = UUID.randomUUID();
        when(environmentRepository.findById(environmentId)).thenReturn(Optional.empty());

        Map<String, Object> result = service.resolve(Map.of(
                "userId", "criteria",
                "email", "criteria",
                "amount", "criteria"), environmentId);

        assertThat(result.get("userId")).isInstanceOf(String.class);
        assertThat(result.get("email").toString()).endsWith("@example.com");
        assertThat(result.get("amount")).isEqualTo(99.99);
    }

    @Test
    void returnsEmptyMapForNullCriteria() {
        assertThat(service.resolve(null, null)).isEmpty();
    }
}
