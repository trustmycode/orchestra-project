package com.orchestra.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication(scanBasePackages = {
        "com.orchestra.api",
        "com.orchestra.domain"
})
@EntityScan(basePackages = "com.orchestra.domain.model")
@EnableJpaRepositories(basePackages = "com.orchestra.domain.repository")
@EnableAsync
@EnableScheduling
public class OrchestraApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestraApiApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(300))
                .build();
    }

}
