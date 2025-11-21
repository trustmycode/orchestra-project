package com.orchestra.api.service;

import com.orchestra.domain.model.*;
import com.orchestra.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class DataResolverServiceTest {

    @Autowired
    private DataResolverService dataResolverService;

    @Autowired
    private EnvironmentRepository environmentRepository;
    @Autowired
    private DbConnectionProfileRepository dbProfileRepository;
    @Autowired
    private DataResolverRepository dataResolverRepository;
    @Autowired
    private TenantRepository tenantRepository;

    @MockBean
    private VectorStore vectorStore;

    private Tenant tenant;
    private Environment environment;
    private DbConnectionProfile dbProfile;

    @BeforeEach
    public void setup() {
        tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName("Test Tenant");
        tenantRepository.save(tenant);

        dbProfile = new DbConnectionProfile();
        dbProfile.setId(UUID.randomUUID());
        dbProfile.setTenant(tenant);
        dbProfile.setName("H2 Profile");
        dbProfile.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dbProfile.setUsername("sa");
        dbProfile.setPassword("");
        dbProfileRepository.save(dbProfile);

        environment = new Environment();
        environment.setId(UUID.randomUUID());
        environment.setTenant(tenant);
        environment.setName("Test Env");
        environment.setProfileMappings(Map.of("db", Map.of("main_db", dbProfile.getId().toString())));
        environmentRepository.save(environment);

        DataSource ds = new DriverManagerDataSource(dbProfile.getJdbcUrl(), dbProfile.getUsername(), dbProfile.getPassword());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (id UUID, username VARCHAR(255), role VARCHAR(50))");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("INSERT INTO users (id, username, role) VALUES ('11111111-1111-1111-1111-111111111111', 'admin_user', 'ADMIN')");
    }

    @Test
    public void testRecursiveResolution() {
        DataResolver resolver = new DataResolver();
        resolver.setId(UUID.randomUUID());
        resolver.setTenant(tenant);
        resolver.setEntityName("admin_user");
        resolver.setDataSource("main_db");
        resolver.setMapping("SELECT id, username FROM users WHERE role = 'ADMIN'");
        dataResolverRepository.save(resolver);

        Map<String, Object> input = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        body.put("admin_user", null);
        input.put("body", body);

        Map<String, Object> result = dataResolverService.resolve(input, environment.getId());

        assertThat(result).isNotNull();
        Map<String, Object> resBody = (Map<String, Object>) result.get("body");
        assertThat(resBody).isNotNull();
        
        Object adminUser = resBody.get("admin_user");
        assertThat(adminUser).isInstanceOf(Map.class);
        Map<String, Object> adminUserMap = (Map<String, Object>) adminUser;
        assertThat(adminUserMap.get("username")).isEqualTo("admin_user");
    }
}

