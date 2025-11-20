package com.orchestra.executor.service.impl;

import com.orchestra.executor.service.SecretProvider;
import org.springframework.stereotype.Service;

@Service
public class EnvSecretProvider implements SecretProvider {

    @Override
    public String resolve(String value) {
        if (value != null && value.startsWith("{{env.") && value.endsWith("}}")) {
            String varName = value.substring(6, value.length() - 2);
            String envValue = System.getenv(varName);
            return envValue != null ? envValue : value;
        }
        return value;
    }
}
