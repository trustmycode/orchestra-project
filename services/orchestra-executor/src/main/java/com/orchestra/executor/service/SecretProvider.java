package com.orchestra.executor.service;

public interface SecretProvider {
    String resolve(String value);
}
