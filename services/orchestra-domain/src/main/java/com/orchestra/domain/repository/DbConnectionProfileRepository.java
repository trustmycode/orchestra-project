package com.orchestra.domain.repository;

import com.orchestra.domain.model.DbConnectionProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DbConnectionProfileRepository extends JpaRepository<DbConnectionProfile, UUID> {
}
