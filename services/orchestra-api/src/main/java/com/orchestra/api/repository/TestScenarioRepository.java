package com.orchestra.api.repository;

import com.orchestra.api.model.TestScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestScenarioRepository extends JpaRepository<TestScenario, UUID> {

    @Query("SELECT ts FROM TestScenario ts " +
            "LEFT JOIN FETCH ts.suite s " +
            "LEFT JOIN FETCH s.process " +
            "LEFT JOIN FETCH s.processVersion")
    @Override
    List<TestScenario> findAll();

    @Query("SELECT ts FROM TestScenario ts " +
            "LEFT JOIN FETCH ts.tenant t " +
            "LEFT JOIN FETCH ts.suite s " +
            "LEFT JOIN FETCH s.process p " +
            "LEFT JOIN FETCH s.processVersion pv " +
            "LEFT JOIN FETCH ts.steps st " +
            "WHERE ts.id = :id")
    Optional<TestScenario> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT ts FROM TestScenario ts " +
            "LEFT JOIN FETCH ts.suite s " +
            "LEFT JOIN FETCH s.process p " +
            "LEFT JOIN FETCH s.processVersion pv " +
            "WHERE s.id = :suiteId")
    List<TestScenario> findBySuiteIdWithSuite(@Param("suiteId") UUID suiteId);
}
