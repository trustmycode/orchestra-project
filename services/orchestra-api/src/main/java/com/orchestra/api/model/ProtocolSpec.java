package com.orchestra.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "protocol_specs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "service_name", "version"})
})
public class ProtocolSpec {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "protocol_id", nullable = false)
    private String protocolId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String version;

    @Column(name = "raw_spec_uri")
    private String rawSpecUri;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parsed_summary")
    private Map<String, Object> parsedSummary;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

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

    public String getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(String protocolId) {
        this.protocolId = protocolId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRawSpecUri() {
        return rawSpecUri;
    }

    public void setRawSpecUri(String rawSpecUri) {
        this.rawSpecUri = rawSpecUri;
    }

    public Map<String, Object> getParsedSummary() {
        return parsedSummary;
    }

    public void setParsedSummary(Map<String, Object> parsedSummary) {
        this.parsedSummary = parsedSummary;
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

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
