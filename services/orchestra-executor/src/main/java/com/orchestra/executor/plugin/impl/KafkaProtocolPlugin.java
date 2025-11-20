package com.orchestra.executor.plugin.impl;

import com.orchestra.domain.model.Environment;
import com.orchestra.domain.model.KafkaClusterProfile;
import com.orchestra.domain.model.ScenarioStep;
import com.orchestra.domain.model.TestRun;
import com.orchestra.domain.repository.KafkaClusterProfileRepository;
import com.orchestra.executor.model.ExecutionContext;
import com.orchestra.executor.plugin.ProtocolPlugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProtocolPlugin implements ProtocolPlugin {

    private final KafkaClusterProfileRepository kafkaClusterProfileRepository;

    @Override
    public boolean supports(String channelType) {
        return "KAFKA".equalsIgnoreCase(channelType);
    }

    @Override
    public void execute(ScenarioStep step, ExecutionContext context, TestRun run) {
        log.info("Executing Kafka step: {} (alias: {})", step.getName(), step.getAlias());

        if (!"ASSERTION".equalsIgnoreCase(step.getKind())) {
            throw new UnsupportedOperationException("Kafka steps currently support only ASSERTION kind");
        }

        Map<String, Object> meta = getActionMeta(step);
        String clusterAlias = (String) meta.get("clusterAlias");
        String topic = (String) meta.get("topic");
        String keyExpression = (String) meta.get("keyExpression");
        String valueExpression = (String) meta.get("valueExpression");
        long timeoutMs = getMetaLong(meta, "timeoutMs", 10_000L);

        if (clusterAlias == null || topic == null) {
            throw new IllegalArgumentException("Kafka step requires both 'clusterAlias' and 'topic'");
        }

        Environment environment = Optional.ofNullable(run.getEnvironment())
                .orElseThrow(() -> new IllegalStateException("Kafka step execution requires TestRun environment"));

        UUID profileId = resolveProfileId(environment, clusterAlias);
        KafkaClusterProfile profile = kafkaClusterProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalStateException("KafkaClusterProfile not found: " + profileId));

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, profile.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "orchestra-check-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // Security configuration can be extended here once profile supports it fully

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(topic));

            long endTime = System.currentTimeMillis() + timeoutMs;
            boolean found = false;

            while (System.currentTimeMillis() < endTime) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    if (matches(record, keyExpression, valueExpression)) {
                        found = true;
                        context.getVariables().put(step.getAlias() + ".found", true);
                        context.getVariables().put(step.getAlias() + ".key", record.key());
                        context.getVariables().put(step.getAlias() + ".value", record.value());
                        log.info("Kafka assertion satisfied for step {}", step.getAlias());
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }

            if (!found) {
                throw new RuntimeException("Kafka assertion failed: message not found on topic " + topic + " in " + timeoutMs + " ms");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getActionMeta(ScenarioStep step) {
        Map<String, Object> action = step.getAction();
        if (action == null || !action.containsKey("meta")) {
            throw new IllegalArgumentException("Kafka step action must include meta section");
        }
        return (Map<String, Object>) action.get("meta");
    }

    @SuppressWarnings("unchecked")
    private UUID resolveProfileId(Environment environment, String alias) {
        Map<String, Object> mappings = environment.getProfileMappings();
        if (mappings == null || !mappings.containsKey("kafka")) {
            throw new IllegalStateException("Environment profile mappings do not contain kafka section");
        }
        Map<String, Object> kafkaMappings = (Map<String, Object>) mappings.get("kafka");
        Object profileId = kafkaMappings.get(alias);
        if (profileId == null) {
            throw new IllegalArgumentException("Kafka profile alias not found in environment mappings: " + alias);
        }
        return UUID.fromString(profileId.toString());
    }

    private long getMetaLong(Map<String, Object> meta, String key, long defaultValue) {
        Object value = meta.get(key);
        return value == null ? defaultValue : Long.parseLong(value.toString());
    }

    private boolean matches(ConsumerRecord<String, String> record, String keyExpression, String valueExpression) {
        if (keyExpression != null && !keyExpression.isEmpty()) {
            if (record.key() == null || !record.key().equals(keyExpression)) {
                return false;
            }
        }
        if (valueExpression != null && !valueExpression.isEmpty()) {
            if (record.value() == null || !record.value().contains(valueExpression)) {
                return false;
            }
        }
        return true;
    }
}
