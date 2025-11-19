package com.orchestra.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String RUN_JOBS_EXCHANGE = "run_jobs_exchange";
    public static final String RUN_JOBS_QUEUE = "run_jobs_queue";
    public static final String RUN_JOBS_ROUTING_KEY = "run_jobs_key";

    public static final String RUN_JOBS_DLX = "run_jobs_dlx";
    public static final String RUN_JOBS_DLQ = "run_jobs_dlq";
    public static final String RUN_JOBS_DLQ_ROUTING_KEY = "run_jobs_dlq_key";

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    // 1. Явное создание ConnectionFactory
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        log.info(">>> Creating ConnectionFactory for: {}:{}", host, port);
        return connectionFactory;
    }

    // 2. Явное создание RabbitAdmin (решает ошибку "No qualifying bean")
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // 3. Инициализатор для принудительного создания очередей
    @Bean
    public ApplicationRunner rabbitInitializer(RabbitAdmin rabbitAdmin) {
        return args -> {
            log.info(">>> [RABBITMQ] Force initialization...");
            try {
                rabbitAdmin.initialize();
                log.info(">>> [RABBITMQ] Queues initialized successfully.");
            } catch (Exception e) {
                log.error(">>> [RABBITMQ] Init failed: " + e.getMessage());
            }
        };
    }

    // === Очереди ===

    @Bean
    DirectExchange runJobsExchange() {
        return new DirectExchange(RUN_JOBS_EXCHANGE);
    }

    @Bean
    DirectExchange runJobsDlx() {
        return new DirectExchange(RUN_JOBS_DLX);
    }

    @Bean
    Queue runJobsQueue() {
        return QueueBuilder.durable(RUN_JOBS_QUEUE)
                .withArgument("x-dead-letter-exchange", RUN_JOBS_DLX)
                .withArgument("x-dead-letter-routing-key", RUN_JOBS_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    Queue runJobsDlq() {
        return new Queue(RUN_JOBS_DLQ);
    }

    @Bean
    Binding runJobsBinding(Queue runJobsQueue, DirectExchange runJobsExchange) {
        return BindingBuilder.bind(runJobsQueue).to(runJobsExchange).with(RUN_JOBS_ROUTING_KEY);
    }

    @Bean
    Binding runJobsDlqBinding(Queue runJobsDlq, DirectExchange runJobsDlx) {
        return BindingBuilder.bind(runJobsDlq).to(runJobsDlx).with(RUN_JOBS_DLQ_ROUTING_KEY);
    }
}