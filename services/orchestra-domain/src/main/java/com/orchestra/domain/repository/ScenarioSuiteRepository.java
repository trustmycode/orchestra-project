package com.orchestra.domain.repository;

import com.orchestra.domain.model.ScenarioSuite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ScenarioSuiteRepository extends JpaRepository<ScenarioSuite, UUID> {
}
