package com.orchestra.domain.repository;

import com.orchestra.domain.model.TestStepResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestStepResultRepository extends JpaRepository<TestStepResult, Long> {
    List<TestStepResult> findByRunIdOrderByStartedAtAsc(UUID runId);
}
