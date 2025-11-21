package com.orchestra.domain.repository;

import com.orchestra.domain.model.TestDataSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestDataSetRepository extends JpaRepository<TestDataSet, UUID> {
    @Query("select tds from TestDataSet tds where tds.tenant.id = :tenantId and tds.scope = :scope")
    List<TestDataSet> findByTenantIdAndScope(
            @Param("tenantId") UUID tenantId,
            @Param("scope") String scope);
}
