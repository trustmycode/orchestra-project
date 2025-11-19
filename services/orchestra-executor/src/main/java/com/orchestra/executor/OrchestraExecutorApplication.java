package com.orchestra.executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.orchestra.executor", "com.orchestra.domain"})
@EntityScan(basePackages = "com.orchestra.domain.model")
@EnableJpaRepositories(basePackages = "com.orchestra.domain.repository")
@EnableScheduling
public class OrchestraExecutorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestraExecutorApplication.class, args);
    }

}
