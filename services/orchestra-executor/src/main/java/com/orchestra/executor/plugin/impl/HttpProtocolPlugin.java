package com.orchestra.executor.plugin.impl;

import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestRun;
import com.orchestra.executor.model.ExecutionContext;
import com.orchestra.executor.plugin.ProtocolPlugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpProtocolPlugin implements ProtocolPlugin {

    private final RestTemplate restTemplate;

    @Override
    public boolean supports(String channelType) {
        return "HTTP_REST".equals(channelType);
    }

    @Override
    public void execute(ScenarioStep step, ExecutionContext context, TestRun run) {
        log.info("Executing HTTP step: {} (Alias: {})", step.getName(), step.getAlias());
        Map<String, Object> action = step.getAction();
        if (action == null || !action.containsKey("inputTemplate")) {
            throw new IllegalArgumentException("Step action or inputTemplate is missing");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> input = (Map<String, Object>) action.get("inputTemplate");

        String methodStr = (String) input.getOrDefault("method", "GET");
        HttpMethod method = HttpMethod.valueOf(methodStr.toUpperCase());

        String urlTemplate = (String) input.get("url");
        if (urlTemplate == null) {
            throw new IllegalArgumentException("URL is missing in inputTemplate");
        }
        String url = resolveTemplate(urlTemplate, context.getVariables());

        HttpHeaders headers = new HttpHeaders();
        if (input.containsKey("headers")) {
            @SuppressWarnings("unchecked")
            Map<String, String> headerMap = (Map<String, String>) input.get("headers");
            headerMap.forEach((k, v) -> headers.add(k, resolveTemplate(v, context.getVariables())));
        }

        Object body = input.get("body");
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(url, method, requestEntity, Object.class);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("statusCode", response.getStatusCode().value());
            responseData.put("body", response.getBody());
            responseData.put("headers", response.getHeaders());

            Map<String, Object> stepOutput = new HashMap<>();
            stepOutput.put("response", responseData);

            context.getVariables().put(step.getAlias(), stepOutput);

            log.info("HTTP Step {} completed with status {}", step.getAlias(), response.getStatusCode());
        } catch (Exception e) {
            log.error("HTTP Step {} failed", step.getAlias(), e);
            throw new RuntimeException("HTTP execution failed: " + e.getMessage(), e);
        }
    }

    private String resolveTemplate(String template, Map<String, Object> variables) {
        if (template == null || !template.contains("{{")) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(key, value);
        }
        return result;
    }
}
