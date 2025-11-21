package com.orchestra.domain.repository;

import com.orchestra.domain.model.ProtocolSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProtocolSpecRepository extends JpaRepository<ProtocolSpec, UUID> {
    @Query("select ps from ProtocolSpec ps where ps.tenant.id = :tenantId and ps.serviceName = :serviceName and ps.version = :version")
    Optional<ProtocolSpec> findByTenantIdAndServiceNameAndVersion(
            @Param("tenantId") UUID tenantId,
            @Param("serviceName") String serviceName,
            @Param("version") String version);

    @Query("select ps from ProtocolSpec ps where ps.tenant.id = :tenantId and ps.serviceName = :serviceName order by ps.createdAt desc")
    List<ProtocolSpec> findByTenantIdAndServiceName(
            @Param("tenantId") UUID tenantId,
            @Param("serviceName") String serviceName);
}
