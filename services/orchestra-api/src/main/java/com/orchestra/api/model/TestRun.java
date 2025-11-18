package com.orchestra.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_runs")
public class TestRun {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private TestScenario scenario;

    @Column(name = "scenario_version", nullable = false)
    private Integer scenarioVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_set_id")
    private TestDataSet dataSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id")
    private Environment environment;

    @Column(nullable = false)
    private String mode;

    @Column(name = "start_step_index")
    private Integer startStepIndex;

    @Column(nullable = false)
    private String status;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "triggered_by")
    private String triggeredBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public TestScenario getScenario() {
        return scenario;
    }

    public void setScenario(TestScenario scenario) {
        this.scenario = scenario;
    }

    public Integer getScenarioVersion() {
        return scenarioVersion;
    }

    public void setScenarioVersion(Integer scenarioVersion) {
        this.scenarioVersion = scenarioVersion;
    }

    public TestDataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(TestDataSet dataSet) {
        this.dataSet = dataSet;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getStartStepIndex() {
        return startStepIndex;
    }

    public void setStartStepIndex(Integer startStepIndex) {
        this.startStepIndex = startStepIndex;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
}
