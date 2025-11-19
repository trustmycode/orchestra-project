package com.orchestra.executor.listener;

import com.orchestra.executor.config.RabbitMQConfig;
import com.orchestra.executor.service.TestRunExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobListener {

    private final TestRunExecutorService testRunExecutorService;

    @RabbitListener(queues = RabbitMQConfig.RUN_JOBS_QUEUE)
    public void receiveMessage(String message) {
        log.info("Received job message: {}", message);
        UUID testRunId;
        try {
            String idString = message.replace("\"", "").trim();
            testRunId = UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in message: {}", message, e);
            throw new AmqpRejectAndDontRequeueException("Invalid UUID format", e);
        }

        try {
            testRunExecutorService.execute(testRunId);
        } catch (Exception e) {
            log.error("Failed to process job for testRunId: {}", testRunId, e);
            throw e;
        }
    }
}
