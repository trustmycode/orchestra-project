package com.orchestra.api.service;

import com.orchestra.domain.dto.ScenarioAnalysisResponse;
import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.Tenant;
import com.orchestra.domain.model.TestScenario;
import com.orchestra.domain.repository.TestScenarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ScenarioAnalyzerService scenarioAnalyzerService;

    @Mock
    private DataResolverService dataResolverService;

    @Mock
    private TestScenarioRepository testScenarioRepository;

    @InjectMocks
    private AiService aiService;

    private UUID scenarioId;
    private UUID environmentId;
    private TestScenario scenario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "aiServiceUrl", "http://ai-service");

        scenarioId = UUID.randomUUID();
        environmentId = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID());

        scenario = new TestScenario();
        scenario.setId(scenarioId);
        scenario.setName("Test Scenario");
        scenario.setTenant(tenant);
        
        ScenarioStep step1 = new ScenarioStep();
        step1.setId(UUID.randomUUID());
        step1.setAlias("step1");
        step1.setKind("ACTION");
        
        ScenarioStep step2 = new ScenarioStep();
        step2.setId(UUID.randomUUID());
        step2.setAlias("step2");
        step2.setKind("ACTION");

        scenario.setSteps(List.of(step1, step2));
    }

    @Test
    void testGenerateDataForScenario_TwoPhaseFlow() {
        // Mock Repository
        when(testScenarioRepository.findByIdWithDetails(scenarioId)).thenReturn(Optional.of(scenario));

        // Mock Phase 1: Analysis
        ScenarioAnalysisResponse analysisResponse = new ScenarioAnalysisResponse(
                List.of(new ScenarioAnalysisResponse.GlobalVariable("USER_ID", "Main User", "UUID"))
        );
        when(scenarioAnalyzerService.analyze(any())).thenReturn(analysisResponse);

        // Mock Phase 2: Planner call for Global Context
        Map<String, Object> plannerResponse = Map.of(
                "result", Map.of("USER_ID", "criteria"),
                "notes", "Plan generated"
        );
        when(restTemplate.postForObject(eq("http://ai-service/api/v1/ai/generate"), any(), eq(Map.class)))
                .thenReturn(plannerResponse);

        // Mock Phase 2: Resolver
        Map<String, Object> resolvedGlobalContext = Map.of("USER_ID", "123-456");
        when(dataResolverService.resolve(any(), eq(environmentId))).thenReturn(resolvedGlobalContext);

        // Mock Phase 3: Step Generation calls
        // Note: In the actual service, it calls generateData() which calls restTemplate again.
        // Since we are mocking restTemplate, we need to handle those calls too.
        // The service calls generateData -> restTemplate.postForObject
        
        // We can just verify that restTemplate is called multiple times.
        // 1 call for Global Context Planner
        // 2 calls for Step Data Planner (one for each step)
        
        aiService.generateDataForScenario(scenarioId, environmentId);

        // Verify Analysis was called
        verify(scenarioAnalyzerService).analyze(any());
        
        // Verify Planner was called at least 3 times (1 global + 2 steps)
        verify(restTemplate, atLeast(3)).postForObject(eq("http://ai-service/api/v1/ai/generate"), any(), eq(Map.class));
        
        // Verify Resolver was called for global context
        verify(dataResolverService).resolve(any(), eq(environmentId));
    }
}

