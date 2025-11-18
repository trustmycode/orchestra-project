package com.orchestra.api.repository;

import com.orchestra.api.model.TestDataSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestDataSetRepository extends JpaRepository<TestDataSet, UUID> {
}
