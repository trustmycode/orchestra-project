package com.orchestra.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "scenario_steps", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"scenario_id", "order_index"}),
        @UniqueConstraint(columnNames = {"scenario_id", "alias"})
})
public class ScenarioStep {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private TestScenario scenario;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String kind;

    @Column(name = "channel_type", nullable = false)
    private String channelType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "endpoint_ref")
    private Map<String, Object> endpointRef;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> action;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> expectations;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TestScenario getScenario() {
        return scenario;
    }

    public void setScenario(TestScenario scenario) {
        this.scenario = scenario;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public Map<String, Object> getEndpointRef() {
        return endpointRef;
    }

    public void setEndpointRef(Map<String, Object> endpointRef) {
        this.endpointRef = endpointRef;
    }

    public Map<String, Object> getAction() {
        return action;
    }

    public void setAction(Map<String, Object> action) {
        this.action = action;
    }

    public Map<String, Object> getExpectations() {
        return expectations;
    }

    public void setExpectations(Map<String, Object> expectations) {
        this.expectations = expectations;
    }
}
