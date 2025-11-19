package com.orchestra.domain.repository;

import com.orchestra.domain.model.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessRepository extends JpaRepository<Process, UUID> {
    @Query("select p from Process p where p.tenant.id = :tenantId and p.key = :key")
    Optional<Process> findByTenantIdAndKey(@Param("tenantId") UUID tenantId, @Param("key") String key);
}
