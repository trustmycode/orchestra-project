package com.orchestra.ai.tool;

import com.orchestra.ai.context.AiContext;
import com.orchestra.domain.model.TestDataSet;
import com.orchestra.domain.repository.TestDataSetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DictionaryLookupTool {

    private final TestDataSetRepository testDataSetRepository;
    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Tool(description = "Look up a value in the global dictionary or configuration data sets.")
    @Transactional(readOnly = true)
    public String lookupValue(@ToolParam(description = "Key to search for in the dictionary") String key) {
        UUID tenantId = AiContext.getTenantId();
        if (tenantId == null) {
            tenantId = DEFAULT_TENANT_ID;
        }

        log.info("Tool: Looking up key '{}' in global datasets for tenant {}", key, tenantId);

        List<TestDataSet> globalSets = testDataSetRepository.findByTenantIdAndScope(tenantId, "GLOBAL");

        for (TestDataSet dataSet : globalSets) {
            if (dataSet.getData() != null && dataSet.getData().containsKey(key)) {
                Object value = dataSet.getData().get(key);
                log.info("Tool: Found value for key '{}'", key);
                return String.valueOf(value);
            }
        }

        log.warn("Tool: Key '{}' not found in any global dataset", key);
        return "Value not found for key: " + key;
    }
}

