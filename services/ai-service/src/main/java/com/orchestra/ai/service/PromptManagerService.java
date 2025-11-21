package com.orchestra.ai.service;

import com.orchestra.domain.model.PromptTemplate;
import com.orchestra.domain.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptManagerService {

    private final PromptTemplateRepository promptTemplateRepository;

    @Cacheable(value = "prompts", key = "#key")
    @Transactional(readOnly = true)
    public String getPrompt(String key) {
        log.debug("Fetching prompt for key: {}", key);
        return promptTemplateRepository.findByKey(key)
                .map(PromptTemplate::getTemplate)
                .orElseThrow(() -> new IllegalArgumentException("Prompt not found for key: " + key));
    }

    @CacheEvict(value = "prompts", key = "#key")
    @Transactional
    public void updatePrompt(String key, String newTemplate) {
        log.info("Updating prompt for key: {}", key);
        PromptTemplate prompt = promptTemplateRepository.findByKey(key)
                .orElseGet(() -> {
                    PromptTemplate p = new PromptTemplate();
                    p.setKey(key);
                    return p;
                });
        prompt.setTemplate(newTemplate);
        Integer currentVersion = prompt.getVersion();
        prompt.setVersion((currentVersion == null ? 0 : currentVersion) + 1);
        promptTemplateRepository.save(prompt);
    }
}

