package com.orchestra.domain.repository;

import com.orchestra.domain.model.SuiteRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface SuiteRunRepository extends JpaRepository<SuiteRun, UUID> {
    List<SuiteRun> findByStatus(String status);

    @Query("SELECT s.context FROM SuiteRun s WHERE s.id = :id")
    Map<String, Object> findContextById(@Param("id") UUID id);

    @Modifying
    @Query(value = "UPDATE suite_runs SET context = coalesce(context, '{}'::jsonb) || cast(:contextJson as jsonb) WHERE id = :id", nativeQuery = true)
    void updateContext(@Param("id") UUID id, @Param("contextJson") String contextJson);
}

