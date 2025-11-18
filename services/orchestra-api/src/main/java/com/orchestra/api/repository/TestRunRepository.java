package com.orchestra.api.repository;

import com.orchestra.api.model.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestRunRepository extends JpaRepository<TestRun, UUID> {
}
