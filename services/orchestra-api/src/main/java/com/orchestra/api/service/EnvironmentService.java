package com.orchestra.api.service;

import com.orchestra.api.exception.ResourceNotFoundException;
import com.orchestra.domain.model.DbConnectionProfile;
import com.orchestra.domain.model.Environment;
import com.orchestra.domain.model.KafkaClusterProfile;
import com.orchestra.domain.model.Tenant;
import com.orchestra.domain.repository.DbConnectionProfileRepository;
import com.orchestra.domain.repository.EnvironmentRepository;
import com.orchestra.domain.repository.KafkaClusterProfileRepository;
import com.orchestra.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final EnvironmentRepository environmentRepository;
    private final DbConnectionProfileRepository dbProfileRepository;
    private final KafkaClusterProfileRepository kafkaProfileRepository;
    private final TenantRepository tenantRepository;

    private Tenant getDefaultTenant() {
        return tenantRepository.findById(DEFAULT_TENANT_ID)
                .orElseThrow(() -> new IllegalStateException("Default tenant not found"));
    }

    public List<Environment> findAllEnvironments() {
        return environmentRepository.findAll();
    }

    @Transactional
    public Environment createEnvironment(Environment env) {
        env.setId(UUID.randomUUID());
        env.setTenant(getDefaultTenant());
        if (env.getProfileMappings() == null) {
            env.setProfileMappings(new HashMap<>());
        }
        return environmentRepository.save(env);
    }

    @Transactional
    public Environment updateEnvironment(UUID id, Environment envUpdate) {
        Environment existing = environmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Environment not found: " + id));

        existing.setName(envUpdate.getName());
        existing.setDescription(envUpdate.getDescription());
        existing.setProfileMappings(envUpdate.getProfileMappings());
        return environmentRepository.save(existing);
    }

    public void deleteEnvironment(UUID id) {
        environmentRepository.deleteById(id);
    }

    public List<DbConnectionProfile> findAllDbProfiles() {
        return dbProfileRepository.findAll();
    }

    @Transactional
    public DbConnectionProfile createDbProfile(DbConnectionProfile profile) {
        profile.setId(UUID.randomUUID());
        profile.setTenant(getDefaultTenant());
        return dbProfileRepository.save(profile);
    }

    public void deleteDbProfile(UUID id) {
        dbProfileRepository.deleteById(id);
    }

    public List<KafkaClusterProfile> findAllKafkaProfiles() {
        return kafkaProfileRepository.findAll();
    }

    @Transactional
    public KafkaClusterProfile createKafkaProfile(KafkaClusterProfile profile) {
        profile.setId(UUID.randomUUID());
        profile.setTenant(getDefaultTenant());
        return kafkaProfileRepository.save(profile);
    }

    public void deleteKafkaProfile(UUID id) {
        kafkaProfileRepository.deleteById(id);
    }
}
