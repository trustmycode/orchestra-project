package com.orchestra.domain.repository;

import com.orchestra.domain.model.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TestRunRepository extends JpaRepository<TestRun, UUID> {

    @Modifying
    @Query("UPDATE TestRun t SET t.lockedBy = :lockedBy, t.lockUntil = :lockUntil, t.heartbeatAt = :now, t.status = 'IN_PROGRESS' WHERE t.id = :id AND (t.lockedBy IS NULL OR t.lockUntil < :now)")
    int acquireLock(@Param("id") UUID id, @Param("lockedBy") String lockedBy, @Param("lockUntil") OffsetDateTime lockUntil, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE TestRun t SET t.heartbeatAt = :heartbeatAt, t.lockUntil = :lockUntil WHERE t.id = :id AND t.lockedBy = :lockedBy")
    int updateHeartbeat(@Param("id") UUID id, @Param("lockedBy") String lockedBy, @Param("heartbeatAt") OffsetDateTime heartbeatAt, @Param("lockUntil") OffsetDateTime lockUntil);

    @Modifying
    @Query("UPDATE TestRun t SET t.status = 'FAILED_STUCK', t.finishedAt = :now WHERE t.status = 'IN_PROGRESS' AND t.heartbeatAt < :threshold")
    int failStuckRuns(@Param("threshold") OffsetDateTime threshold, @Param("now") OffsetDateTime now);

    @Query("SELECT t FROM TestRun t WHERE t.suiteRun.id = :suiteRunId")
    List<TestRun> findBySuiteRunId(@Param("suiteRunId") UUID suiteRunId);
}
