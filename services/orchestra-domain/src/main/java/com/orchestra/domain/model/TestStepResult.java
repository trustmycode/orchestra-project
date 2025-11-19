package com.orchestra.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "test_step_results")
public class TestStepResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private TestRun run;

    @Column(name = "step_id", nullable = false)
    private UUID stepId;

    @Column(name = "step_alias", nullable = false)
    private String stepAlias;

    @Column(nullable = false)
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> violations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_context_snapshot")
    private Map<String, Object> inputContextSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_context_delta")
    private Map<String, Object> outputContextDelta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestRun getRun() {
        return run;
    }

    public void setRun(TestRun run) {
        this.run = run;
    }

    public UUID getStepId() {
        return stepId;
    }

    public void setStepId(UUID stepId) {
        this.stepId = stepId;
    }

    public String getStepAlias() {
        return stepAlias;
    }

    public void setStepAlias(String stepAlias) {
        this.stepAlias = stepAlias;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Map<String, Object> getViolations() {
        return violations;
    }

    public void setViolations(Map<String, Object> violations) {
        this.violations = violations;
    }

    public Map<String, Object> getInputContextSnapshot() {
        return inputContextSnapshot;
    }

    public void setInputContextSnapshot(Map<String, Object> inputContextSnapshot) {
        this.inputContextSnapshot = inputContextSnapshot;
    }

    public Map<String, Object> getOutputContextDelta() {
        return outputContextDelta;
    }

    public void setOutputContextDelta(Map<String, Object> outputContextDelta) {
        this.outputContextDelta = outputContextDelta;
    }
}
