package com.orchestra.api.repository;

import com.orchestra.api.model.Process;
import com.orchestra.api.model.ProcessVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessVersionRepository extends JpaRepository<ProcessVersion, UUID> {
    Optional<ProcessVersion> findTopByProcessOrderByVersionDesc(Process process);
}
