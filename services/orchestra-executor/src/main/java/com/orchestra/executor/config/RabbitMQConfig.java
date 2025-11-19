package com.orchestra.executor.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String RUN_JOBS_QUEUE = "run_jobs_queue";
}
