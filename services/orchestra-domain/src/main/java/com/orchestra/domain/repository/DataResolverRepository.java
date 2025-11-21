package com.orchestra.domain.repository;

import com.orchestra.domain.model.DataResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataResolverRepository extends JpaRepository<DataResolver, UUID> {
    Optional<DataResolver> findByTenantIdAndEntityName(UUID tenantId, String entityName);
    List<DataResolver> findAllByTenantId(UUID tenantId);
}

